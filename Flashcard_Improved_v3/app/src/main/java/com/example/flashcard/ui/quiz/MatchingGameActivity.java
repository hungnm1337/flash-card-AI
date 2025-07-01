package com.example.flashcard.ui.quiz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.score.ScoreActivity;

import java.util.ArrayList;
import java.util.Collections;

public class MatchingGameActivity extends AppCompatActivity implements MatchingAdapter.OnCardClickListener {
    private ArrayList<WordModel> wordModelArrayList;
    private ArrayList<MatchingCard> matchingCards;
    private RecyclerView recyclerView;
    private MatchingAdapter adapter;
    private TextView scoreText;
    
    private MatchingCard firstSelectedCard = null;
    private MatchingCard secondSelectedCard = null;
    private int matchedPairs = 0;
    private int attempts = 0;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_game);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Matching Game - " + getFolderNameFromSharedPreferences());

        initViews();
        loadWords();
        setupGame();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.matchingRecyclerView);
        scoreText = findViewById(R.id.scoreText);
        
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
    }

    private void loadWords() {
        DBHandler dbHandler = new DBHandler(this);
        int folderId = getFolderIdFromSharedPreferences();
        ArrayList<WordModel> allWords = dbHandler.getWordsByFolderId(folderId);
        
        if (allWords.size() < 3) {
            Toast.makeText(this, "Need at least 3 words to play matching game!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Take only first 6 words for the game (12 cards total)
        wordModelArrayList = new ArrayList<>();
        int wordCount = Math.min(6, allWords.size());
        for (int i = 0; i < wordCount; i++) {
            wordModelArrayList.add(allWords.get(i));
        }
    }

    private void setupGame() {
        matchingCards = new ArrayList<>();
        
        // Create word cards
        for (WordModel word : wordModelArrayList) {
            matchingCards.add(new MatchingCard(word.getWord(), MatchingCard.CardType.WORD, word.getWordId()));
            matchingCards.add(new MatchingCard(word.getMeaning(), MatchingCard.CardType.MEANING, word.getWordId()));
        }
        
        // Shuffle the cards
        Collections.shuffle(matchingCards);
        
        adapter = new MatchingAdapter(matchingCards, this);
        recyclerView.setAdapter(adapter);
        
        updateScore();
    }

    @Override
    public void onCardClick(MatchingCard card, int position) {
        if (isProcessing || card.isMatched() || card.isFlipped()) {
            return;
        }

        card.setFlipped(true);
        adapter.notifyItemChanged(position);

        if (firstSelectedCard == null) {
            firstSelectedCard = card;
        } else if (secondSelectedCard == null) {
            secondSelectedCard = card;
            attempts++;
            checkMatch();
        }
    }

    private void checkMatch() {
        isProcessing = true;
        
        new Handler().postDelayed(() -> {
            if (firstSelectedCard.getWordId() == secondSelectedCard.getWordId() &&
                firstSelectedCard.getType() != secondSelectedCard.getType()) {
                // Match found
                firstSelectedCard.setMatched(true);
                secondSelectedCard.setMatched(true);
                matchedPairs++;
                
                Toast.makeText(this, "Match found!", Toast.LENGTH_SHORT).show();
                
                if (matchedPairs == wordModelArrayList.size()) {
                    // Game completed
                    showResults();
                }
            } else {
                // No match, flip cards back
                firstSelectedCard.setFlipped(false);
                secondSelectedCard.setFlipped(false);
            }
            
            adapter.notifyDataSetChanged();
            firstSelectedCard = null;
            secondSelectedCard = null;
            isProcessing = false;
            updateScore();
        }, 1000);
    }

    private void updateScore() {
        scoreText.setText("Matches: " + matchedPairs + "/" + wordModelArrayList.size() + 
                         " | Attempts: " + attempts);
    }

    private void showResults() {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("correctCount", matchedPairs);
        intent.putExtra("incorrectCount", attempts - matchedPairs);
        intent.putExtra("gameMode", "Matching Game");
        startActivity(intent);
        finish();
    }

    private String getFolderNameFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getString(getString(R.string.current_folder_name), "Words");
    }

    private int getFolderIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.current_folder_id), -1);
    }
}


