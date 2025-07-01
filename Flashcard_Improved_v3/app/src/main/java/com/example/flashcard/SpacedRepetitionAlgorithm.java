package com.example.flashcard;

import com.example.flashcard.modal.WordModel;

public class SpacedRepetitionAlgorithm {

    private static final double DEFAULT_EASE_FACTOR = 2.5;
    private static final int[] INTERVALS = {1, 6, 15, 30, 60, 120}; // in days

    public static void calculateNextReview(WordModel word, int quality) {
        // Quality: 0-2 (incorrect), 3 (hard), 4 (good), 5 (easy)

        int repetitions = word.getRepetitions();
        double easeFactor = word.getEaseFactor();
        int interval = word.getInterval();

        if (quality >= 3) { // Correct answer
            repetitions++;
            easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            if (easeFactor < 1.3) {
                easeFactor = 1.3;
            }

            if (repetitions == 1) {
                interval = 1;
            } else if (repetitions == 2) {
                interval = 6;
            } else {
                interval = (int) Math.round(interval * easeFactor);
            }
        } else { // Incorrect answer
            repetitions = 0;
            interval = 1;
        }

        long nextReviewDate = System.currentTimeMillis() + (long) interval * 24 * 60 * 60 * 1000; // Convert days to milliseconds

        word.setRepetitions(repetitions);
        word.setEaseFactor(easeFactor);
        word.setInterval(interval);
        word.setNextReviewDate(nextReviewDate);
    }
}


