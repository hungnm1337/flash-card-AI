package com.example.flashcard.ui.tts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.score.ScoreActivity;

import java.util.ArrayList;
import java.util.Locale;

public class TTSActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ArrayList<WordModel> wordModelArrayList;
    private int currentWordIndex = 0;
    private TextToSpeech textToSpeech;
    private boolean isTTSReady = false;

    private TextView wordText;
    private TextView meaningText;
    private Button playWordButton;
    private Button playMeaningButton;
    private Button nextButton;
    private Button prevButton;
    private TextView wordCounter;
    private SeekBar speedSeekBar;
    private TextView speedText;

    private int correctCount = 0;
    private int totalWords = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Listen & Learn - " + getFolderNameFromSharedPreferences());

        initViews();
        initTTS();
        loadWords();
        showWord();
        setupListeners();
    }

    private void initViews() {
        wordText = findViewById(R.id.wordText);
        meaningText = findViewById(R.id.meaningText);
        playWordButton = findViewById(R.id.playWordButton);
        playMeaningButton = findViewById(R.id.playMeaningButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        wordCounter = findViewById(R.id.wordCounter);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        speedText = findViewById(R.id.speedText);

        // Initially disable TTS buttons
        playWordButton.setEnabled(false);
        playMeaningButton.setEnabled(false);
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSActivity", "Language not supported or missing data");
                Toast.makeText(this, "Language not supported. Please install TTS data.", Toast.LENGTH_LONG).show();
            } else {
                isTTSReady = true;
                playWordButton.setEnabled(true);
                playMeaningButton.setEnabled(true);
                textToSpeech.setSpeechRate(1.0f);
            }
        } else {
            Log.e("TTSActivity", "TTS initialization failed with status: " + status);
            Toast.makeText(this, "TTS initialization failed. Please check your TTS engine.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadWords() {
        DBHandler dbHandler = new DBHandler(this);
        int folderId = getFolderIdFromSharedPreferences();
        wordModelArrayList = dbHandler.getWordsByFolderId(folderId);
        
        if (wordModelArrayList.isEmpty()) {
            Toast.makeText(this, "No words found in this folder!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        totalWords = wordModelArrayList.size();
        currentWordIndex = 0;
    }

    private void showWord() {
        if (wordModelArrayList.isEmpty()) return;
        
        WordModel currentWord = wordModelArrayList.get(currentWordIndex);
        wordText.setText(currentWord.getWord());
        meaningText.setText(currentWord.getMeaning());
        
        updateWordCounter();
        updateNavigationButtons();
    }

    private void updateWordCounter() {
        wordCounter.setText((currentWordIndex + 1) + " / " + totalWords);
    }

    private void updateNavigationButtons() {
        prevButton.setEnabled(currentWordIndex > 0);
        nextButton.setEnabled(currentWordIndex < totalWords - 1);
    }

    private void setupListeners() {
        playWordButton.setOnClickListener(v -> speakText(wordText.getText().toString()));
        
        playMeaningButton.setOnClickListener(v -> speakText(meaningText.getText().toString()));
        
        nextButton.setOnClickListener(v -> {
            if (currentWordIndex < totalWords - 1) {
                currentWordIndex++;
                showWord();
            }
        });
        
        prevButton.setOnClickListener(v -> {
            if (currentWordIndex > 0) {
                currentWordIndex--;
                showWord();
            }
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = 0.5f + (progress / 100.0f) * 1.5f; // Range: 0.5x to 2.0x
                speedText.setText(String.format("Speed: %.1fx", speed));
                if (isTTSReady) {
                    textToSpeech.setSpeechRate(speed);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set initial speed
        speedSeekBar.setProgress(33); // Default to 1.0x speed
    }

    private void speakText(String text) {
        if (isTTSReady && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Toast.makeText(this, "Text-to-Speech not ready", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFolderNameFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getString(getString(R.string.current_folder_name), "Words");
    }

    private int getFolderIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.current_folder_id), -1);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        super.onPause();
    }
}


