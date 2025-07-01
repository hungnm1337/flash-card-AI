package com.example.flashcard.ui.ai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.wordlist.WordList;
import com.example.flashcard.ai.AIFlashcardGenerator;

import java.util.ArrayList;
import java.util.List;

public class AIGeneratorActivity extends AppCompatActivity {
    private EditText etTopic, etCount;
    private Button btnGenerate, btnSaveAll;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private GeneratedWordAdapter adapter;
    private List<GeneratedWord> generatedWords;
    private DBHandler dbHandler;
    private AIFlashcardGenerator aiFlashcardGenerator;
    private long folderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_generator);

        initViews();
        setupRecyclerView();
        
        dbHandler = new DBHandler(this);
        aiFlashcardGenerator = new AIFlashcardGenerator(this);
        
        // Get folder ID from intent, nếu không có thì lấy từ SharedPreferences
        folderId = getIntent().getLongExtra("folder_id", -1);
        if (folderId == -1) {
            // Lấy từ SharedPreferences nếu không truyền qua intent
            folderId = getFolderIdFromSharedPreferences();
        }
        
        btnGenerate.setOnClickListener(v -> generateWords());
        btnSaveAll.setOnClickListener(v -> saveAllWords());
    }

    private void initViews() {
        etTopic = findViewById(R.id.et_topic);
        etCount = findViewById(R.id.et_count);
        btnGenerate = findViewById(R.id.btn_generate);
        btnSaveAll = findViewById(R.id.btn_save_all);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        generatedWords = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new GeneratedWordAdapter(generatedWords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void generateWords() {
        String topic = etTopic.getText().toString().trim();
        String countStr = etCount.getText().toString().trim();

        if (TextUtils.isEmpty(topic)) {
            Toast.makeText(this, "Vui lòng nhập chủ đề", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = 10; // default
        if (!TextUtils.isEmpty(countStr)) {
            try {
                count = Integer.parseInt(countStr);
                if (count <= 0 || count > 50) {
                    Toast.makeText(this, "Số lượng từ phải từ 1-50", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số lượng từ không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showLoading(true);
        aiFlashcardGenerator.generateFlashcards(topic, count, (int)folderId, new AIFlashcardGenerator.FlashcardGenerationCallback() {
            @Override
            public void onSuccess(List<WordModel> flashcards) {
                runOnUiThread(() -> {
                    showLoading(false);
                    generatedWords.clear();
                    for (WordModel wordModel : flashcards) {
                        generatedWords.add(new GeneratedWord(wordModel.getWord(), wordModel.getMeaning(), wordModel.getPronunciation()));
                    }
                    adapter.notifyDataSetChanged();
                    btnSaveAll.setVisibility(View.VISIBLE);
                    Toast.makeText(AIGeneratorActivity.this, 
                        "Đã tạo " + flashcards.size() + " từ vựng", 
                        Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(AIGeneratorActivity.this, 
                        "Lỗi: " + error, 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveAllWords() {
        if (generatedWords.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng nào để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (folderId == -1) {
            Toast.makeText(this, "Không xác định được thư mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Words are already saved by AIFlashcardGenerator, just show success message
        Toast.makeText(AIGeneratorActivity.this, 
            "Đã lưu " + generatedWords.size() + " từ vựng", 
            Toast.LENGTH_SHORT).show();
        
        // Return to word list
        Intent intent = new Intent(AIGeneratorActivity.this, WordList.class);
        intent.putExtra("folder_id", folderId);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!show);
    }

    private int getFolderIdFromSharedPreferences() {
        return getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE)
                .getInt(getString(R.string.current_folder_id), -1);
    }
}