package com.example.flashcard.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class StudyReminderWorker extends Worker {

    public StudyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
            notificationHelper.showStudyReminder();
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}

