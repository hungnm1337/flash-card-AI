package com.example.flashcard.ui.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.WordModel;
import com.example.flashcard.ui.score.ScoreActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {
    private ArrayList<WordModel> wordModelArrayList;
    private ArrayList<QuizQuestion> quizQuestions;
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private int incorrectCount = 0;

    private TextView questionText;
    private RadioGroup optionsRadioGroup;
    private RadioButton option1Radio, option2Radio, option3Radio, option4Radio;
    private Button submitButton;
    private TextView questionCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Quiz - " + getFolderNameFromSharedPreferences());

        initViews();
        loadWords();
        generateQuizQuestions();
        showQuestion();

        submitButton.setOnClickListener(v -> checkAnswer());
    }

    private void initViews() {
        questionText = findViewById(R.id.questionText);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        option1Radio = findViewById(R.id.option1Radio);
        option2Radio = findViewById(R.id.option2Radio);
        option3Radio = findViewById(R.id.option3Radio);
        option4Radio = findViewById(R.id.option4Radio);
        submitButton = findViewById(R.id.submitButton);
        questionCounter = findViewById(R.id.questionCounter);
    }

    private void loadWords() {
        DBHandler dbHandler = new DBHandler(this);
        int folderId = getFolderIdFromSharedPreferences();
        wordModelArrayList = dbHandler.getWordsByFolderId(folderId);
        
        if (wordModelArrayList.size() < 4) {
            Toast.makeText(this, "Need at least 4 words to start quiz!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void generateQuizQuestions() {
        quizQuestions = new ArrayList<>();
        Collections.shuffle(wordModelArrayList);
        
        int questionCount = Math.min(10, wordModelArrayList.size()); // Max 10 questions
        
        for (int i = 0; i < questionCount; i++) {
            WordModel correctWord = wordModelArrayList.get(i);
            ArrayList<String> options = new ArrayList<>();
            options.add(correctWord.getMeaning());
            
            // Add wrong answers from other words
            ArrayList<WordModel> otherWords = new ArrayList<>(wordModelArrayList);
            otherWords.remove(correctWord);
            Collections.shuffle(otherWords);
            
            // Add up to 3 wrong answers, but handle case where we have fewer words
            int wrongAnswersNeeded = Math.min(3, otherWords.size());
            for (int j = 0; j < wrongAnswersNeeded; j++) {
                String wrongAnswer = otherWords.get(j).getMeaning();
                // Avoid duplicate meanings
                if (!options.contains(wrongAnswer)) {
                    options.add(wrongAnswer);
                }
            }
            
            // If we still don't have 4 options, add some generic wrong answers
            while (options.size() < 4) {
                String[] genericWrongAnswers = {
                    "Không có nghĩa này",
                    "Đáp án khác",
                    "Không đúng",
                    "Sai rồi"
                };
                for (String generic : genericWrongAnswers) {
                    if (!options.contains(generic) && options.size() < 4) {
                        options.add(generic);
                    }
                }
                // Prevent infinite loop
                if (options.size() >= 4) break;
            }
            
            // Ensure we have exactly 4 options
            while (options.size() > 4) {
                options.remove(options.size() - 1);
            }
            
            Collections.shuffle(options);
            int correctIndex = options.indexOf(correctWord.getMeaning());
            
            QuizQuestion question = new QuizQuestion(
                "What is the meaning of: " + correctWord.getWord(),
                options,
                correctIndex
            );
            quizQuestions.add(question);
        }
    }

    private void showQuestion() {
        if (currentQuestionIndex >= quizQuestions.size()) {
            showResults();
            return;
        }

        QuizQuestion question = quizQuestions.get(currentQuestionIndex);
        questionText.setText(question.getQuestion());
        
        ArrayList<String> options = question.getOptions();
        option1Radio.setText(options.get(0));
        option2Radio.setText(options.get(1));
        option3Radio.setText(options.get(2));
        option4Radio.setText(options.get(3));
        
        optionsRadioGroup.clearCheck();
        submitButton.setEnabled(true);
        
        questionCounter.setText((currentQuestionIndex + 1) + "/" + quizQuestions.size());
    }

    private void checkAnswer() {
        int selectedId = optionsRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = -1;
        if (selectedId == R.id.option1Radio) selectedIndex = 0;
        else if (selectedId == R.id.option2Radio) selectedIndex = 1;
        else if (selectedId == R.id.option3Radio) selectedIndex = 2;
        else if (selectedId == R.id.option4Radio) selectedIndex = 3;

        QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
        
        if (selectedIndex == currentQuestion.getCorrectIndex()) {
            correctCount++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            incorrectCount++;
            Toast.makeText(this, "Wrong! Correct answer: " + 
                currentQuestion.getOptions().get(currentQuestion.getCorrectIndex()), 
                Toast.LENGTH_LONG).show();
        }

        currentQuestionIndex++;
        submitButton.setEnabled(false);
        
        // Show next question after a delay
        questionText.postDelayed(() -> showQuestion(), 1500);
    }

    private void showResults() {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("correctCount", correctCount);
        intent.putExtra("incorrectCount", incorrectCount);
        intent.putExtra("quizMode", true);
        startActivity(intent);
        finish();
    }

    private int getFolderIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.current_folder_id), -1);
    }

    private String getFolderNameFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getString(getString(R.string.current_folder_name), "Words");
    }

    // Inner class for quiz questions
    private static class QuizQuestion {
        private String question;
        private ArrayList<String> options;
        private int correctIndex;

        public QuizQuestion(String question, ArrayList<String> options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }

        public String getQuestion() { return question; }
        public ArrayList<String> getOptions() { return options; }
        public int getCorrectIndex() { return correctIndex; }
    }
}


