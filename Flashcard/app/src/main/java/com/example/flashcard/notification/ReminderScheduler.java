package com.example.flashcard.notification;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ReminderScheduler {
    private static final String WORK_TAG = "study_reminder_work";
    
    private Context context;
    private WorkManager workManager;

    public ReminderScheduler(Context context) {
        this.context = context;
        this.workManager = WorkManager.getInstance(context);
    }

    public void scheduleReminders(int intervalHours) {
        // Cancel existing work
        workManager.cancelAllWorkByTag(WORK_TAG);
        
        // Create new periodic work request
        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                StudyReminderWorker.class,
                intervalHours,
                TimeUnit.HOURS
        )
        .addTag(WORK_TAG)
        .build();

        // Enqueue the work
        workManager.enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderWork
        );
    }

    public void cancelReminders() {
        workManager.cancelAllWorkByTag(WORK_TAG);
    }

    public static void scheduleDefaultReminders(Context context) {
        ReminderScheduler scheduler = new ReminderScheduler(context);
        scheduler.scheduleReminders(4); // Default: every 4 hours
    }
}

