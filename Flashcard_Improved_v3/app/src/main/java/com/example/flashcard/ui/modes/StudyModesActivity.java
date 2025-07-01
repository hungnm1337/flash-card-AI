package com.example.flashcard.ui.modes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.ui.card.CardFlip;
import com.example.flashcard.ui.quiz.QuizActivity;
import com.example.flashcard.ui.quiz.MatchingGameActivity;
import com.example.flashcard.ui.tts.TTSActivity;
import com.example.flashcard.ui.tts.ListeningQuizActivity;
import com.example.flashcard.ui.filter.FilterSortActivity;
import com.example.flashcard.ui.folders.FolderListActivity;
import com.example.flashcard.ui.statistics.StatisticsActivity;

import java.util.ArrayList;

public class StudyModesActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_modes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DBHandler dbHandler = new DBHandler(this);
        if (dbHandler.getFolderNames().isEmpty()) {
            Toast.makeText(this, "Please create a folder first!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, FolderListActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Check if a folder is selected
        int folderId = getFolderIdFromSharedPreferences();
        if (folderId == -1) {
            Toast.makeText(this, "Please select a folder first!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, FolderListActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setTitle("Study Modes - " + getFolderNameFromSharedPreferences());

        setupModeCards();
    }

    private void setupModeCards() {
        CardView flashcardMode = findViewById(R.id.flashcardModeCard);
        CardView quizMode = findViewById(R.id.quizModeCard);
        CardView matchingMode = findViewById(R.id.matchingModeCard);
        CardView listeningMode = findViewById(R.id.listeningModeCard);
        CardView listeningQuizMode = findViewById(R.id.listeningQuizModeCard);
        CardView filterSortMode = findViewById(R.id.filterSortModeCard);
        CardView statisticsMode = findViewById(R.id.statisticsModeCard);

        flashcardMode.setOnClickListener(v -> {
            Intent intent = new Intent(StudyModesActivity.this, CardFlip.class);
            startActivity(intent);
        });

        quizMode.setOnClickListener(v -> {
            Intent intent = new Intent(StudyModesActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        matchingMode.setOnClickListener(v -> {
            Intent intent = new Intent(StudyModesActivity.this, MatchingGameActivity.class);
            startActivity(intent);
        });

        listeningMode.setOnClickListener(v -> {
            Intent intent = new Intent(StudyModesActivity.this, TTSActivity.class);
            startActivity(intent);
        });

        listeningQuizMode.setOnClickListener(v -> {
            Intent intent = new Intent(StudyModesActivity.this, ListeningQuizActivity.class);
            startActivity(intent);
        });

        filterSortMode.setOnClickListener(v -> {
            Intent intent = new Intent(StudyModesActivity.this, FilterSortActivity.class);
            startActivity(intent);
        });

        statisticsMode.setOnClickListener(v -> {
            Intent intent = new Intent(StudyModesActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
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


