package com.example.flashcard.ui.ai;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flashcard.R;
import com.example.flashcard.ai.AIFlashcardGenerator;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.wordlist.WordList;

import java.util.List;

public class AIFlashcardActivity extends AppCompatActivity {
    private EditText topicEditText;
    private EditText countEditText;
    private Button generateButton;
    private AIFlashcardGenerator aiGenerator;
    private int folderId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_flashcard);

        // Get folder ID from intent
        folderId = getIntent().getIntExtra("folder_id", -1);
        if (folderId == -1) {
            Toast.makeText(this, "Error: No folder selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        
        aiGenerator = new AIFlashcardGenerator(this);
    }

    private void initViews() {
        topicEditText = findViewById(R.id.topicEditText);
        countEditText = findViewById(R.id.countEditText);
        generateButton = findViewById(R.id.generateButton);
        
        // Set default count
        countEditText.setText("5");
    }

    private void setupClickListeners() {
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateFlashcards();
            }
        });
    }

    private void generateFlashcards() {
        String topic = topicEditText.getText().toString().trim();
        String countStr = countEditText.getText().toString().trim();

        if (topic.isEmpty()) {
            Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show();
            return;
        }

        int count;
        try {
            count = Integer.parseInt(countStr);
            if (count <= 0 || count > 20) {
                Toast.makeText(this, "Please enter a count between 1 and 20", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating flashcards with AI...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Generate flashcards
        aiGenerator.generateFlashcards(topic, count, folderId, new AIFlashcardGenerator.FlashcardGenerationCallback() {
            @Override
            public void onSuccess(List<WordModel> flashcards) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(AIFlashcardActivity.this, 
                            "Generated " + flashcards.size() + " flashcards successfully!", 
                            Toast.LENGTH_LONG).show();
                        
                        // Return to word list
                        Intent intent = new Intent(AIFlashcardActivity.this, WordList.class);
                        intent.putExtra("folder_id", folderId);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(AIFlashcardActivity.this, 
                            "Error generating flashcards: " + error, 
                            Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}

