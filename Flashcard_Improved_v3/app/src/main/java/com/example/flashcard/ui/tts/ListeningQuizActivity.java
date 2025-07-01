package com.example.flashcard.ui.tts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.score.ScoreActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class ListeningQuizActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ArrayList<WordModel> wordModelArrayList;
    private ArrayList<WordModel> quizWords;
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private TextToSpeech textToSpeech;
    private boolean isTTSReady = false;

    private TextView instructionText;
    private Button playButton;
    private EditText answerInput;
    private Button submitButton;
    private TextView questionCounter;
    private TextView feedbackText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening_quiz);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Listening Quiz - " + getFolderNameFromSharedPreferences());

        initViews();
        initTTS();
        loadWords();
        setupQuiz();
        setupListeners();
    }

    private void initViews() {
        instructionText = findViewById(R.id.instructionText);
        playButton = findViewById(R.id.playButton);
        answerInput = findViewById(R.id.answerInput);
        submitButton = findViewById(R.id.submitButton);
        questionCounter = findViewById(R.id.questionCounter);
        feedbackText = findViewById(R.id.feedbackText);

        playButton.setEnabled(false);
        instructionText.setText("Listen to the pronunciation and type what you hear:");
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("ListeningQuizActivity", "Language not supported or missing data");
                Toast.makeText(this, "Language not supported. Please install TTS data.", Toast.LENGTH_LONG).show();
            } else {
                isTTSReady = true;
                playButton.setEnabled(true);
                textToSpeech.setSpeechRate(0.8f);
                showQuestion();
            }
        } else {
            Log.e("ListeningQuizActivity", "TTS initialization failed with status: " + status);
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
    }

    private void setupQuiz() {
        quizWords = new ArrayList<>(wordModelArrayList);
        Collections.shuffle(quizWords);
        
        // Limit to 10 questions max
        int questionCount = Math.min(10, quizWords.size());
        quizWords = new ArrayList<>(quizWords.subList(0, questionCount));
        
        currentQuestionIndex = 0;
    }

    private void setupListeners() {
        playButton.setOnClickListener(v -> playCurrentWord());
        
        submitButton.setOnClickListener(v -> checkAnswer());
        
        answerInput.setOnEditorActionListener((v, actionId, event) -> {
            checkAnswer();
            return true;
        });
    }

    private void showQuestion() {
        if (currentQuestionIndex >= quizWords.size()) {
            showResults();
            return;
        }

        questionCounter.setText((currentQuestionIndex + 1) + " / " + quizWords.size());
        answerInput.setText("");
        feedbackText.setText("");
        answerInput.setEnabled(true);
        submitButton.setEnabled(true);
        
        // Auto-play the word when question is shown
        playCurrentWord();
    }

    private void playCurrentWord() {
        if (isTTSReady && currentQuestionIndex < quizWords.size()) {
            String wordToSpeak = quizWords.get(currentQuestionIndex).getWord();
            textToSpeech.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void checkAnswer() {
        String userAnswer = answerInput.getText().toString().trim();
        String correctAnswer = quizWords.get(currentQuestionIndex).getWord();
        
        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "Please enter your answer", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCorrect = userAnswer.equalsIgnoreCase(correctAnswer);
        
        if (isCorrect) {
            correctCount++;
            feedbackText.setText("Correct! ✓");
            feedbackText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            incorrectCount++;
            feedbackText.setText("Incorrect. The correct answer is: " + correctAnswer);
            feedbackText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        answerInput.setEnabled(false);
        submitButton.setEnabled(false);
        
        // Show next question after delay
        feedbackText.postDelayed(() -> {
            currentQuestionIndex++;
            showQuestion();
        }, 2000);
    }

    private void showResults() {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("correctCount", correctCount);
        intent.putExtra("incorrectCount", incorrectCount);
        intent.putExtra("gameMode", "Listening Quiz");
        startActivity(intent);
        finish();
    }

    private String getFolderNameFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getString(getString(R.string.current_folder_name), "Words");
    }

    private int getFolderIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.current_folder_id), -1);
    }
}

