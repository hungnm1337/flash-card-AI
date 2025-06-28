package com.example.flashcard.ui.wordlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.ai.AIFlashcardActivity;
import com.example.flashcard.ui.card.CardFlip;
import com.example.flashcard.ui.form.WordFormActivity;

import java.util.ArrayList;

public class WordList extends AppCompatActivity {

    private ArrayList<WordModel> wordModelArrayList;
    private RecyclerView recyclerView;
    private Button btnReview;
    private Button btnAI;
    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getFolderNameFromSharedPreferences());

        recyclerView = findViewById(R.id.wordListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        wordModelArrayList = new ArrayList<>();
        dbHandler = new DBHandler(this);
        int folderId = getFolderIdFromSharedPreferences();
        wordModelArrayList = dbHandler.getWordsByFolderId(folderId);

        if (wordModelArrayList.isEmpty()) {
            findViewById(R.id.btnReview).setVisibility(View.GONE);
            findViewById(R.id.emptyListMessage).setVisibility(View.VISIBLE);
        }
        else{
            findViewById(R.id.btnReview).setVisibility(View.VISIBLE);
            findViewById(R.id.emptyListMessage).setVisibility(View.GONE);
        }

        WordListAdapter wordListAdapter = new WordListAdapter(wordModelArrayList, this);
        recyclerView.setAdapter(wordListAdapter);

        btnReview = findViewById(R.id.btnReview);
        btnAI = findViewById(R.id.btnAI);

        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WordList.this, CardFlip.class);
                startActivity(intent);
            }
        });

        btnAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WordList.this, AIFlashcardActivity.class);
                intent.putExtra("folder_id", folderId);
                startActivity(intent);
            }
        });

        findViewById(R.id.fabAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WordList.this, WordFormActivity.class);
                startActivity(intent);
            }
        });
    }

    private int getFolderIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.current_folder_id), -1);
    }

    private String getFolderNameFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getString(getString(R.string.current_folder_name), "Words");
    }
}