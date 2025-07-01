package com.example.flashcard.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;

import com.example.flashcard.R;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "FlashcardSettings";
    private static final String THEME_KEY = "app_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Settings");

        RadioGroup themeRadioGroup = findViewById(R.id.themeRadioGroup);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedTheme = preferences.getInt(THEME_KEY, R.style.Theme_Flashcard);

        if (savedTheme == R.style.Theme_Flashcard) {
            themeRadioGroup.check(R.id.radioDefaultTheme);
        } else if (savedTheme == R.style.Theme_Flashcard_Dark) {
            themeRadioGroup.check(R.id.radioDarkTheme);
        } else if (savedTheme == R.style.Theme_Flashcard_Light) {
            themeRadioGroup.check(R.id.radioLightTheme);
        }

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedTheme = R.style.Theme_Flashcard;
            if (checkedId == R.id.radioDefaultTheme) {
                selectedTheme = R.style.Theme_Flashcard;
            } else if (checkedId == R.id.radioDarkTheme) {
                selectedTheme = R.style.Theme_Flashcard_Dark;
            } else if (checkedId == R.id.radioLightTheme) {
                selectedTheme = R.style.Theme_Flashcard_Light;
            }
            preferences.edit().putInt(THEME_KEY, selectedTheme).apply();
            recreate(); // Recreate activity to apply theme change
        });
    }

    public static int getSelectedTheme(SharedPreferences preferences) {
        return preferences.getInt(THEME_KEY, R.style.Theme_Flashcard);
    }
}

