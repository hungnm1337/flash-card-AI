package com.example.flashcard.ai;

import android.content.Context;
import android.util.Log;

import com.example.flashcard.api.GeminiApiService;
import com.example.flashcard.api.GeminiRequest;
import com.example.flashcard.api.GeminiResponse;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AIFlashcardGenerator {
    private static final String TAG = "AIFlashcardGenerator";
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/";
    private static final String API_KEY = "AIzaSyBdmQEBS2YWfpzWX0VR6LqWfuV6CuIqb2Y"; // Replace with actual API key
    
    private GeminiApiService apiService;
    private DBHandler dbHandler;
    private Context context;

    public AIFlashcardGenerator(Context context) {
        this.context = context;
        this.dbHandler = new DBHandler(context);
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GEMINI_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        this.apiService = retrofit.create(GeminiApiService.class);
    }

    public interface FlashcardGenerationCallback {
        void onSuccess(List<WordModel> flashcards);
        void onError(String error);
    }

    public void generateFlashcards(String topic, int count, int folderId, FlashcardGenerationCallback callback) {
        String prompt = String.format(
            "Generate %d flashcards about '%s'. " +
            "Format each flashcard as 'WORD: DEFINITION' on separate lines. " +
            "Make sure the words are relevant to the topic and definitions are clear and concise. " +
            "Example format:\n" +
            "Apple: A round fruit that is typically red, green, or yellow\n" +
            "Tree: A woody perennial plant with a trunk and branches",
            count, topic
        );

        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(Arrays.asList(part));
        GeminiRequest request = new GeminiRequest(Arrays.asList(content));

        Call<GeminiResponse> call = apiService.generateContent(API_KEY, request);
        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseText = response.body().getCandidates().get(0)
                                .getContent().getParts().get(0).getText();
                        
                        List<WordModel> flashcards = parseFlashcards(responseText, folderId);
                        
                        // Save to database
                        for (WordModel flashcard : flashcards) {
                            dbHandler.addWord(folderId, flashcard.getWord(), flashcard.getMeaning());
                        }
                        
                        callback.onSuccess(flashcards);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        callback.onError("Error parsing AI response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "API call failed: " + response.code());
                    callback.onError("API call failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private List<WordModel> parseFlashcards(String responseText, int folderId) {
        List<WordModel> flashcards = new ArrayList<>();
        String[] lines = responseText.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.contains(":") && !line.isEmpty()) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String word = parts[0].trim();
                    String meaning = parts[1].trim();
                    
                    if (!word.isEmpty() && !meaning.isEmpty()) {
                        flashcards.add(new WordModel(folderId, 0, word, meaning));
                    }
                }
            }
        }
        
        return flashcards;
    }
}

