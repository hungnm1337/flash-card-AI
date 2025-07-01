package com.example.flashcard.ui.filter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.folders.FolderListActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FilterSortActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FilteredWordAdapter adapter;
    private ArrayList<WordModel> allWords;
    private ArrayList<WordModel> filteredWords;
    private EditText searchEditText;
    private Spinner sortSpinner;
    private TextView resultCountText;

    private enum SortOption {
        ALPHABETICAL("Alphabetical (A-Z)"),
        REVERSE_ALPHABETICAL("Alphabetical (Z-A)"),
        DATE_ADDED("Date Added (Newest)"),
        DATE_ADDED_OLD("Date Added (Oldest)"),
        DIFFICULTY("Difficulty (Hard first)"),
        DIFFICULTY_EASY("Difficulty (Easy first)"),
        REVIEW_DUE("Review Due (Urgent first)");

        private final String displayName;

        SortOption(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_sort);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Filter & Sort Words");

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
        loadWords();
        setupSpinner();
        setupSearch();
        setupRecyclerView();
        applyFiltersAndSort();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.filteredWordsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        sortSpinner = findViewById(R.id.sortSpinner);
        resultCountText = findViewById(R.id.resultCountText);
    }

    private void loadWords() {
        DBHandler dbHandler = new DBHandler(this);
        int folderId = getFolderIdFromSharedPreferences();
        allWords = dbHandler.getWordsByFolderId(folderId);
        filteredWords = new ArrayList<>(allWords);
    }

    private void setupSpinner() {
        ArrayAdapter<SortOption> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, SortOption.values());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new FilteredWordAdapter(filteredWords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void applyFiltersAndSort() {
        String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
        SortOption selectedSort = (SortOption) sortSpinner.getSelectedItem();

        // Filter words based on search query
        filteredWords.clear();
        for (WordModel word : allWords) {
            if (searchQuery.isEmpty() || 
                word.getWord().toLowerCase().contains(searchQuery) ||
                word.getMeaning().toLowerCase().contains(searchQuery)) {
                filteredWords.add(word);
            }
        }

        // Sort filtered words
        sortWords(filteredWords, selectedSort);

        // Update UI
        adapter.notifyDataSetChanged();
        updateResultCount();
    }

    private void sortWords(ArrayList<WordModel> words, SortOption sortOption) {
        switch (sortOption) {
            case ALPHABETICAL:
                Collections.sort(words, (w1, w2) -> w1.getWord().compareToIgnoreCase(w2.getWord()));
                break;
            case REVERSE_ALPHABETICAL:
                Collections.sort(words, (w1, w2) -> w2.getWord().compareToIgnoreCase(w1.getWord()));
                break;
            case DATE_ADDED:
                Collections.sort(words, (w1, w2) -> Integer.compare(w2.getWordId(), w1.getWordId()));
                break;
            case DATE_ADDED_OLD:
                Collections.sort(words, (w1, w2) -> Integer.compare(w1.getWordId(), w2.getWordId()));
                break;
            case DIFFICULTY:
                Collections.sort(words, (w1, w2) -> {
                    // Lower ease factor = more difficult
                    return Double.compare(w1.getEaseFactor(), w2.getEaseFactor());
                });
                break;
            case DIFFICULTY_EASY:
                Collections.sort(words, (w1, w2) -> {
                    // Higher ease factor = easier
                    return Double.compare(w2.getEaseFactor(), w1.getEaseFactor());
                });
                break;
            case REVIEW_DUE:
                Collections.sort(words, (w1, w2) -> {
                    long currentTime = System.currentTimeMillis();
                    boolean w1Due = w1.getNextReviewDate() <= currentTime;
                    boolean w2Due = w2.getNextReviewDate() <= currentTime;
                    
                    if (w1Due && !w2Due) return -1;
                    if (!w1Due && w2Due) return 1;
                    if (w1Due && w2Due) {
                        return Long.compare(w1.getNextReviewDate(), w2.getNextReviewDate());
                    }
                    return Long.compare(w1.getNextReviewDate(), w2.getNextReviewDate());
                });
                break;
        }
    }

    private void updateResultCount() {
        String countText = filteredWords.size() + " of " + allWords.size() + " words";
        resultCountText.setText(countText);
    }

    private int getFolderIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.current_folder_id), -1);
    }
}


