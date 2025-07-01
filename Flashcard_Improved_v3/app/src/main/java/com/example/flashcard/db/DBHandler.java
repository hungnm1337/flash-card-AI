package com.example.flashcard.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.flashcard.modal.FolderModal;
import com.example.flashcard.modal.WordModel;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "flashcard";
    private static final int DB_VERSION = 3;

    private static final String FOLDERS_TABLE_NAME = "folders";
    private static final String WORDS_TABLE_NAME = "words";
    private static final String ID_COL = "id";
    private static final String FOLDER_ID_COL = "folder_id";
    private static final String FOLDER_NAME_COL = "folder_name";
    private static final String WORD_COL = "word";
    private static final String DESCRIPTION_COL = "description";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFoldersTable(db);
        createWordsTable(db);
    }

    private void createFoldersTable(SQLiteDatabase db) {
        String foldersQuery = "CREATE TABLE " + FOLDERS_TABLE_NAME + "(" +
                FOLDER_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FOLDER_NAME_COL + " TEXT)";
        db.execSQL(foldersQuery);
    }

    private void createWordsTable(SQLiteDatabase db) {
        String wordsQuery = "CREATE TABLE " + WORDS_TABLE_NAME + "(" +
                ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WORD_COL + " TEXT," +
                DESCRIPTION_COL + " TEXT," +
                FOLDER_ID_COL + " INTEGER," +
                "next_review_date INTEGER DEFAULT 0," +
                "repetitions INTEGER DEFAULT 0," +
                "ease_factor REAL DEFAULT 2.5," +
                "interval INTEGER DEFAULT 0," +
                "image_path TEXT," +
                "audio_path TEXT," +
                "FOREIGN KEY(" + FOLDER_ID_COL + ") REFERENCES " + FOLDERS_TABLE_NAME + "(" + FOLDER_ID_COL + "))";
        db.execSQL(wordsQuery);
    }

    public void addFolder(String folderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FOLDER_NAME_COL, folderName);
        db.insert(FOLDERS_TABLE_NAME, null, cv);
        db.close();
    }

    public ArrayList<FolderModal> getFolderNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT * FROM " + FOLDERS_TABLE_NAME, null);

        ArrayList<FolderModal> folderArrayList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int folderId = cursor.getInt(cursor.getColumnIndexOrThrow(FOLDER_ID_COL));
                String folderName = cursor.getString(cursor.getColumnIndexOrThrow(FOLDER_NAME_COL));
                folderArrayList.add(new FolderModal(folderId, folderName));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return folderArrayList;
    }

    public ArrayList<WordModel> getWordsByFolderId(int folderId) {
        ArrayList<WordModel> wordList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {ID_COL, WORD_COL, DESCRIPTION_COL, "next_review_date", "repetitions", "ease_factor", "interval", "image_path", "audio_path"};
        String selection = FOLDER_ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(folderId)};

        Cursor cursor = db.query(WORDS_TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int wordIdIdx = cursor.getColumnIndex(ID_COL);
                int wordIdx = cursor.getColumnIndex(WORD_COL);
                int descIdx = cursor.getColumnIndex(DESCRIPTION_COL);
                int nextReviewIdx = cursor.getColumnIndex("next_review_date");
                int repetitionsIdx = cursor.getColumnIndex("repetitions");
                int easeFactorIdx = cursor.getColumnIndex("ease_factor");
                int intervalIdx = cursor.getColumnIndex("interval");
                int imagePathIdx = cursor.getColumnIndex("image_path");
                int audioPathIdx = cursor.getColumnIndex("audio_path");
                if (wordIdIdx == -1 || wordIdx == -1 || descIdx == -1 || nextReviewIdx == -1 || repetitionsIdx == -1 || easeFactorIdx == -1 || intervalIdx == -1 || imagePathIdx == -1 || audioPathIdx == -1) {
                    continue;
                }
                int wordId = cursor.getInt(wordIdIdx);
                String word = cursor.getString(wordIdx);
                String description = cursor.getString(descIdx);
                long nextReviewDate = cursor.getLong(nextReviewIdx);
                int repetitions = cursor.getInt(repetitionsIdx);
                double easeFactor = cursor.getDouble(easeFactorIdx);
                int interval = cursor.getInt(intervalIdx);
                String imagePath = cursor.getString(imagePathIdx);
                String audioPath = cursor.getString(audioPathIdx);
                String meaning = description;
                String pronunciation = "";
                if (description != null && description.contains("[") && description.endsWith("]")) {
                    int lastOpenBracket = description.lastIndexOf("[");
                    if (lastOpenBracket != -1) {
                        meaning = description.substring(0, lastOpenBracket).trim();
                        pronunciation = description.substring(lastOpenBracket + 1, description.length() - 1).trim();
                    }
                }
                wordList.add(new WordModel(folderId, wordId, word, meaning, pronunciation, nextReviewDate, repetitions, easeFactor, interval, imagePath, audioPath));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return wordList;
    }

    public void addWord(int folderId, String word, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FOLDER_ID_COL, folderId);
        cv.put(WORD_COL, word);
        cv.put(DESCRIPTION_COL, description);
        // Initial values for spaced repetition
        cv.put("next_review_date", System.currentTimeMillis()); // Set to current time for immediate review
        cv.put("repetitions", 0);
        cv.put("ease_factor", 2.5);
        cv.put("interval", 0);
        db.insert(WORDS_TABLE_NAME, null, cv);
        db.close();
    }

    public int getTotalWordsInAFolder(int folderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalWords = 0;
        String[] projection = {ID_COL};
        String selection = FOLDER_ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(folderId)};

        Cursor cursor = db.query(WORDS_TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            totalWords = cursor.getCount();
            cursor.close();
        }
        db.close();
        return totalWords;
    }

    public void updateFolderName(int folderId, String newFolderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FOLDER_NAME_COL, newFolderName);
        db.update(FOLDERS_TABLE_NAME, values, FOLDER_ID_COL + " = ?", new String[]{String.valueOf(folderId)});
        db.close();
    }

    public void deleteFolder(int folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteWordsInFolder(db, folderId);
        db.delete(FOLDERS_TABLE_NAME, FOLDER_ID_COL + "=?", new String[]{String.valueOf(folderId)});
        db.close();
    }
    private void deleteWordsInFolder(SQLiteDatabase db, int folderId) {
        db.delete(WORDS_TABLE_NAME, FOLDER_ID_COL + "=?", new String[]{String.valueOf(folderId)});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new columns for spaced repetition
            db.execSQL("ALTER TABLE " + WORDS_TABLE_NAME + " ADD COLUMN next_review_date INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + WORDS_TABLE_NAME + " ADD COLUMN repetitions INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + WORDS_TABLE_NAME + " ADD COLUMN ease_factor REAL DEFAULT 2.5");
            db.execSQL("ALTER TABLE " + WORDS_TABLE_NAME + " ADD COLUMN interval INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            // Add new columns for media
            db.execSQL("ALTER TABLE " + WORDS_TABLE_NAME + " ADD COLUMN image_path TEXT");
            db.execSQL("ALTER TABLE " + WORDS_TABLE_NAME + " ADD COLUMN audio_path TEXT");
        }
    }

    public void updateWordSpacedRepetition(int wordId, long nextReviewDate, int repetitions, double easeFactor, int interval) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("next_review_date", nextReviewDate);
        values.put("repetitions", repetitions);
        values.put("ease_factor", easeFactor);
        values.put("interval", interval);
        db.update(WORDS_TABLE_NAME, values, ID_COL + " = ?", new String[]{String.valueOf(wordId)});
        db.close();
    }

    public void updateWordMedia(int wordId, String imagePath, String audioPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image_path", imagePath);
        values.put("audio_path", audioPath);
        db.update(WORDS_TABLE_NAME, values, ID_COL + " = ?", new String[]{String.valueOf(wordId)});
        db.close();
    }
}

