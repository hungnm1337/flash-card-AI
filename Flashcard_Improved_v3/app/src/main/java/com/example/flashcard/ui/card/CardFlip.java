package com.example.flashcard.ui.card;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.form.WordFormActivity;
import com.example.flashcard.ui.score.ScoreActivity;
import com.example.flashcard.ui.wordlist.WordList;
import com.example.flashcard.SpacedRepetitionAlgorithm;

import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

public class CardFlip extends AppCompatActivity {
    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;
    private boolean mIsBackVisible = false;
    private View mCardFrontLayout;
    private View mCardBackLayout;
    private int currentWordIndex;
    private ArrayList<WordModel> wordModelArrayList;

    private int correctCount = 0;
    private int incorrectCount = 0;

    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_flip);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getFolderNameFromSharedPreferences());

        dbHandler = new DBHandler(this);

        findViews();
        loadAnimations();
        changeCameraDistance();
        loadWords();
        
        if (wordModelArrayList.isEmpty()) {
            Toast.makeText(this, "No words to review or no words in this folder! Please add words.", Toast.LENGTH_LONG).show();
            finish(); 
            return;
        }
        showWord();

        Button btnAgain = findViewById(R.id.btnAgain);
        Button btnHard = findViewById(R.id.btnHard);
        Button btnGood = findViewById(R.id.btnGood);
        Button btnEasy = findViewById(R.id.btnEasy);

        btnAgain.setOnClickListener(view -> processAnswer(0));
        btnHard.setOnClickListener(view -> processAnswer(3));
        btnGood.setOnClickListener(view -> processAnswer(4));
        btnEasy.setOnClickListener(view -> processAnswer(5));
    }

    private void processAnswer(int quality) {
        WordModel currentWord = wordModelArrayList.get(currentWordIndex);
        SpacedRepetitionAlgorithm.calculateNextReview(currentWord, quality);
        dbHandler.updateWordSpacedRepetition(currentWord.getWordId(), currentWord.getNextReviewDate(),
                currentWord.getRepetitions(), currentWord.getEaseFactor(), currentWord.getInterval());

        if (quality < 3) {
            incorrectCount++;
        } else {
            correctCount++;
        }
        showNextWordOrScore();
    }

    private void changeCameraDistance() {
        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        mCardFrontLayout.setCameraDistance(scale);
        mCardBackLayout.setCameraDistance(scale);
    }

    private void loadAnimations() {
        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_out_animation);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_in_animation);
    }

    private void findViews() {
        mCardBackLayout = findViewById(R.id.card_back);
        mCardFrontLayout = findViewById(R.id.card_front);
    }

    public void flipCard(View view) {
        if (!mIsBackVisible) {
            mSetRightOut.setTarget(mCardFrontLayout);
            mSetLeftIn.setTarget(mCardBackLayout);
            mSetRightOut.start();
            mSetLeftIn.start();
            mIsBackVisible = true;
        } else {
            mSetRightOut.setTarget(mCardBackLayout);
            mSetLeftIn.setTarget(mCardFrontLayout);
            mSetRightOut.start();
            mSetLeftIn.start();
            mIsBackVisible = false;
        }
    }

    public void checkIfFlippedCard(){
        if (mIsBackVisible) {
            mSetRightOut.setTarget(mCardBackLayout);
            mSetLeftIn.setTarget(mCardFrontLayout);
            mSetRightOut.start();
            mSetLeftIn.start();
            mIsBackVisible = false;
        }
    }

    private void loadWords() {
        int folderId = getFolderIdFromSharedPreferences();
        ArrayList<WordModel> allWords = dbHandler.getWordsByFolderId(folderId);
        long currentTime = System.currentTimeMillis();

        // Check if the intent specifies to load all words (e.g., from WordList)
        boolean loadAllWords = getIntent().getBooleanExtra("loadAllWords", false);

        if (loadAllWords) {
            wordModelArrayList = allWords;
        } else {
            // Default behavior: only load words due for review today
            wordModelArrayList = (ArrayList<WordModel>) allWords.stream()
                    .filter(word -> word.getNextReviewDate() <= currentTime)
                    .collect(Collectors.toList());
        }
    }

    private void showWord() {
        if (wordModelArrayList.isEmpty()) {
            return; // Should not happen if loadWords handles it correctly
        }
        TextView cardTextFront = findViewById(R.id.card_text_front);
        TextView cardTextBack = findViewById(R.id.card_text_back);
        cardTextFront.setText(wordModelArrayList.get(currentWordIndex).getWord());
        cardTextBack.setText(wordModelArrayList.get(currentWordIndex).getMeaning());
        showWordIndex();
    }

    private void showWordIndex() {
        TextView wordIndex = findViewById(R.id.wordIndex);
        int currentIndex = currentWordIndex + 1;
        int totalWords = wordModelArrayList.size();
        String indexText = getString(R.string.index_format, currentIndex, totalWords);
        wordIndex.setText(indexText);
    }

    private int getFolderIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.current_folder_id), -1);
    }

    private String getFolderNameFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getString(getString(R.string.current_folder_name), "Words");
    }

    private void showNextWordOrScore() {
        if (currentWordIndex < wordModelArrayList.size() - 1) {
            currentWordIndex++;
            checkIfFlippedCard();
            showWord();
        } else {
            showScores();
        }
    }

    private void showScores() {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("correctCount", correctCount);
        intent.putExtra("incorrectCount", incorrectCount);
        startActivity(intent);
        finish();
    }
}


