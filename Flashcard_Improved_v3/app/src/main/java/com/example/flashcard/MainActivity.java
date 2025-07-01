package com.example.flashcard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.flashcard.ui.folders.FolderListActivity;
import com.example.flashcard.ui.settings.SettingsActivity;
import com.example.flashcard.ui.modes.StudyModesActivity;
import com.example.flashcard.ui.filter.FilterSortActivity;
import com.example.flashcard.ui.statistics.StatisticsActivity;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private int currentThemeResId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        currentThemeResId = SettingsActivity.getSelectedTheme(preferences);
        setTheme(currentThemeResId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button viewFlashcardBtn = findViewById(R.id.viewFlashcardBtn);
        Button studyModesBtn = findViewById(R.id.studyModesBtn);
        Button filterSortBtn = findViewById(R.id.filterSortBtn);
        Button statisticsBtn = findViewById(R.id.statisticsBtn);
        Button settingsBtn = findViewById(R.id.settingsBtn);

        viewFlashcardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FolderListActivity.class);
                startActivity(intent);
            }
        });

        studyModesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StudyModesActivity.class);
                startActivity(intent);
            }
        });

        filterSortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FilterSortActivity.class);
                startActivity(intent);
            }
        });

        statisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        int newThemeResId = SettingsActivity.getSelectedTheme(preferences);
        if (newThemeResId != currentThemeResId) {
            recreate();
        }
    }
}

