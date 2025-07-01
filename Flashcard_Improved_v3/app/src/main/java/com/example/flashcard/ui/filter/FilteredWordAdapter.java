package com.example.flashcard.ui.filter;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.modal.WordModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FilteredWordAdapter extends RecyclerView.Adapter<FilteredWordAdapter.WordViewHolder> {

    private ArrayList<WordModel> words;

    public FilteredWordAdapter(ArrayList<WordModel> words) {
        this.words = words;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filtered_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        WordModel word = words.get(position);
        
        holder.wordText.setText(word.getWord());
        holder.meaningText.setText(word.getMeaning());
        
        // Show difficulty level
        double easeFactor = word.getEaseFactor();
        String difficulty;
        int difficultyColor;
        
        if (easeFactor < 2.0) {
            difficulty = "Hard";
            difficultyColor = R.color.difficulty_hard;
        } else if (easeFactor < 2.5) {
            difficulty = "Medium";
            difficultyColor = R.color.difficulty_medium;
        } else {
            difficulty = "Easy";
            difficultyColor = R.color.difficulty_easy;
        }
        
        holder.difficultyText.setText(difficulty);
        holder.difficultyText.setTextColor(
            ContextCompat.getColor(holder.itemView.getContext(), difficultyColor));
        
        // Show review status
        long currentTime = System.currentTimeMillis();
        if (word.getNextReviewDate() <= currentTime) {
            holder.reviewStatusText.setText("Due for review");
            holder.reviewStatusText.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.review_due));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            String nextReviewDate = sdf.format(new Date(word.getNextReviewDate()));
            holder.reviewStatusText.setText("Next: " + nextReviewDate);
            holder.reviewStatusText.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.review_scheduled));
        }
        
        // Show repetitions count
        holder.repetitionsText.setText("Reps: " + word.getRepetitions());
        
        // Show media indicators
        if (word.getImagePath() != null && !word.getImagePath().isEmpty()) {
            holder.imageIndicator.setVisibility(View.VISIBLE);
            // Optionally show thumbnail
            try {
                holder.imageIndicator.setImageBitmap(
                    BitmapFactory.decodeFile(word.getImagePath()));
            } catch (Exception e) {
                holder.imageIndicator.setImageResource(R.drawable.ic_image);
            }
        } else {
            holder.imageIndicator.setVisibility(View.GONE);
        }
        
        if (word.getAudioPath() != null && !word.getAudioPath().isEmpty()) {
            holder.audioIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.audioIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView wordText;
        TextView meaningText;
        TextView difficultyText;
        TextView reviewStatusText;
        TextView repetitionsText;
        ImageView imageIndicator;
        ImageView audioIndicator;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.wordCardView);
            wordText = itemView.findViewById(R.id.wordText);
            meaningText = itemView.findViewById(R.id.meaningText);
            difficultyText = itemView.findViewById(R.id.difficultyText);
            reviewStatusText = itemView.findViewById(R.id.reviewStatusText);
            repetitionsText = itemView.findViewById(R.id.repetitionsText);
            imageIndicator = itemView.findViewById(R.id.imageIndicator);
            audioIndicator = itemView.findViewById(R.id.audioIndicator);
        }
    }
}

