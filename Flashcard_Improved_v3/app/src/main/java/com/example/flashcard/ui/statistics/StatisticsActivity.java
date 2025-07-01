package com.example.flashcard.ui.statistics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.folders.FolderListActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView totalWordsText;
    private TextView dueForReviewText;
    private TextView masteredWordsText;
    private TextView averageDifficultyText;
    private TextView studyStreakText;
    private TextView wordsWithMediaText;
    private TextView lastStudyDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Study Statistics");

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

        initViews();
        loadStatistics();
    }

    private void initViews() {
        totalWordsText = findViewById(R.id.totalWordsText);
        dueForReviewText = findViewById(R.id.dueForReviewText);
        masteredWordsText = findViewById(R.id.masteredWordsText);
        averageDifficultyText = findViewById(R.id.averageDifficultyText);
        studyStreakText = findViewById(R.id.studyStreakText);
        wordsWithMediaText = findViewById(R.id.wordsWithMediaText);
        lastStudyDateText = findViewById(R.id.lastStudyDateText);
    }

    private void loadStatistics() {
        DBHandler dbHandler = new DBHandler(this);
        int folderId = getFolderIdFromSharedPreferences();
        ArrayList<WordModel> allWords = dbHandler.getWordsByFolderId(folderId);

        if (allWords.isEmpty()) {
            setEmptyStatistics();
            return;
        }

        // Calculate statistics
        int totalWords = allWords.size();
        int dueForReview = 0;
        int masteredWords = 0;
        int wordsWithMedia = 0;
        double totalEaseFactor = 0;
        long currentTime = System.currentTimeMillis();

        for (WordModel word : allWords) {
            // Count due for review
            if (word.getNextReviewDate() <= currentTime) {
                dueForReview++;
            }

            // Count mastered words (high ease factor and multiple repetitions)
            if (word.getEaseFactor() >= 2.8 && word.getRepetitions() >= 5) {
                masteredWords++;
            }

            // Count words with media
            if ((word.getImagePath() != null && !word.getImagePath().isEmpty()) ||
                (word.getAudioPath() != null && !word.getAudioPath().isEmpty())) {
                wordsWithMedia++;
            }

            totalEaseFactor += word.getEaseFactor();
        }

        double averageEaseFactor = totalEaseFactor / totalWords;
        String averageDifficulty = getDifficultyLevel(averageEaseFactor);

        // Get study streak and last study date from preferences
        SharedPreferences prefs = getSharedPreferences("StudyStats", MODE_PRIVATE);
        int studyStreak = prefs.getInt("study_streak", 0);
        long lastStudyTime = prefs.getLong("last_study_date", 0);

        // Update UI
        totalWordsText.setText(String.valueOf(totalWords));
        dueForReviewText.setText(String.valueOf(dueForReview));
        masteredWordsText.setText(String.valueOf(masteredWords));
        averageDifficultyText.setText(averageDifficulty);
        studyStreakText.setText(studyStreak + " days");
        wordsWithMediaText.setText(wordsWithMedia + " / " + totalWords);

        if (lastStudyTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            lastStudyDateText.setText(sdf.format(new Date(lastStudyTime)));
        } else {
            lastStudyDateText.setText("Never");
        }
    }

    private void setEmptyStatistics() {
        totalWordsText.setText("0");
        dueForReviewText.setText("0");
        masteredWordsText.setText("0");
        averageDifficultyText.setText("N/A");
        studyStreakText.setText("0 days");
        wordsWithMediaText.setText("0 / 0");
        lastStudyDateText.setText("Never");
    }

    private String getDifficultyLevel(double easeFactor) {
        if (easeFactor < 2.0) {
            return "Hard";
        } else if (easeFactor < 2.5) {
            return "Medium";
        } else {
            return "Easy";
        }
    }

    private int getFolderIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.current_folder_id), -1);
    }

    public static void updateStudyStreak(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("StudyStats", MODE_PRIVATE);
        long lastStudyTime = prefs.getLong("last_study_date", 0);
        long currentTime = System.currentTimeMillis();
        
        // Check if it\'s a new day
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long daysSinceLastStudy = (currentTime - lastStudyTime) / oneDayInMillis;
        
        int currentStreak = prefs.getInt("study_streak", 0);
        
        if (daysSinceLastStudy == 1) {
            // Consecutive day - increment streak
            currentStreak++;
        } else if (daysSinceLastStudy > 1) {
            // Missed days - reset streak
            currentStreak = 1;
        }
        // If daysSinceLastStudy == 0, it\'s the same day, don\'t change streak
        
        prefs.edit()
            .putLong("last_study_date", currentTime)
            .putInt("study_streak", currentStreak)
            .apply();
    }
}


