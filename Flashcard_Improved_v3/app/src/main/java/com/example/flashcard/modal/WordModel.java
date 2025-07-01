package com.example.flashcard.modal;

public class WordModel {

    private int folderId;
    private int wordId;
    private String word;
    private String meaning;
    private String pronunciation;
    private long nextReviewDate;
    private int repetitions;
    private double easeFactor;
    private int interval;
    private String imagePath;
    private String audioPath;

    // Constructor for new version with spaced repetition fields and media
    public WordModel(int folderId, int wordId, String word, String meaning, String pronunciation, long nextReviewDate, int repetitions, double easeFactor, int interval, String imagePath, String audioPath) {
        this.folderId = folderId;
        this.wordId = wordId;
        this.word = word;
        this.meaning = meaning;
        this.pronunciation = pronunciation;
        this.nextReviewDate = nextReviewDate;
        this.repetitions = repetitions;
        this.easeFactor = easeFactor;
        this.interval = interval;
        this.imagePath = imagePath;
        this.audioPath = audioPath;
    }

    // Constructor for spaced repetition without media
    public WordModel(int folderId, int wordId, String word, String meaning, String pronunciation, long nextReviewDate, int repetitions, double easeFactor, int interval) {
        this(folderId, wordId, word, meaning, pronunciation, nextReviewDate, repetitions, easeFactor, interval, null, null);
    }

    // Constructor for backward compatibility
    public WordModel(int folderId, int wordId, String word, String meaning) {
        this(folderId, wordId, word, meaning, "", System.currentTimeMillis(), 0, 2.5, 0, null, null);
    }

    public int getFolderId() { return folderId; }
    public int getWordId() { return wordId; }
    public String getWord() { return word; }
    public String getMeaning() { return meaning; }
    public String getPronunciation() { return pronunciation; }
    public long getNextReviewDate() { return nextReviewDate; }
    public int getRepetitions() { return repetitions; }
    public double getEaseFactor() { return easeFactor; }
    public int getInterval() { return interval; }
    public String getImagePath() { return imagePath; }
    public String getAudioPath() { return audioPath; }

    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }
    public void setNextReviewDate(long nextReviewDate) { this.nextReviewDate = nextReviewDate; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }
    public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }
    public void setInterval(int interval) { this.interval = interval; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
}

