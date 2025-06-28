package com.example.flashcard.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flashcard.R;
import com.example.flashcard.notification.ReminderScheduler;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "flashcard_settings";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_REMINDER_INTERVAL = "reminder_interval";
    
    private Switch notificationSwitch;
    private SeekBar intervalSeekBar;
    private TextView intervalText;
    private Button saveButton;
    
    private SharedPreferences sharedPreferences;
    private ReminderScheduler reminderScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Notification Settings");
        
        initViews();
        loadSettings();
        setupListeners();
        
        reminderScheduler = new ReminderScheduler(this);
    }

    private void initViews() {
        notificationSwitch = findViewById(R.id.notificationSwitch);
        intervalSeekBar = findViewById(R.id.intervalSeekBar);
        intervalText = findViewById(R.id.intervalText);
        saveButton = findViewById(R.id.saveButton);
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Setup seekbar (1-12 hours)
        intervalSeekBar.setMax(11); // 0-11 represents 1-12 hours
    }

    private void loadSettings() {
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false);
        int reminderInterval = sharedPreferences.getInt(KEY_REMINDER_INTERVAL, 4);
        
        notificationSwitch.setChecked(notificationsEnabled);
        intervalSeekBar.setProgress(reminderInterval - 1); // Convert to 0-based index
        updateIntervalText(reminderInterval);
        
        intervalSeekBar.setEnabled(notificationsEnabled);
    }

    private void setupListeners() {
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                intervalSeekBar.setEnabled(isChecked);
                if (!isChecked) {
                    reminderScheduler.cancelReminders();
                }
            }
        });

        intervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int hours = progress + 1; // Convert to 1-12 hours
                updateIntervalText(hours);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void updateIntervalText(int hours) {
        intervalText.setText("Every " + hours + " hour" + (hours > 1 ? "s" : ""));
    }

    private void saveSettings() {
        boolean notificationsEnabled = notificationSwitch.isChecked();
        int reminderInterval = intervalSeekBar.getProgress() + 1; // Convert to 1-12 hours
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, notificationsEnabled);
        editor.putInt(KEY_REMINDER_INTERVAL, reminderInterval);
        editor.apply();
        
        if (notificationsEnabled) {
            reminderScheduler.scheduleReminders(reminderInterval);
            Toast.makeText(this, "Notifications enabled! You'll receive reminders every " + 
                    reminderInterval + " hour" + (reminderInterval > 1 ? "s" : ""), 
                    Toast.LENGTH_LONG).show();
        } else {
            reminderScheduler.cancelReminders();
            Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
        }
        
        finish();
    }
}

