package com.example.flashcard.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;

import java.util.List;

public class GeneratedWordAdapter extends RecyclerView.Adapter<GeneratedWordAdapter.ViewHolder> {
    private List<GeneratedWord> words;

    public GeneratedWordAdapter(List<GeneratedWord> words) {
        this.words = words;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_generated_word, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GeneratedWord word = words.get(position);
        holder.tvWord.setText(word.getWord());
        holder.tvMeaning.setText(word.getMeaning());
        holder.tvPronunciation.setText(word.getPronunciation());
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning, tvPronunciation;

        ViewHolder(View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            tvPronunciation = itemView.findViewById(R.id.tv_pronunciation);
        }
    }
}

