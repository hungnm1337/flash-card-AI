package com.example.flashcard.ui.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;

import java.util.ArrayList;

public class MatchingAdapter extends RecyclerView.Adapter<MatchingAdapter.MatchingViewHolder> {
    private ArrayList<MatchingCard> cards;
    private OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(MatchingCard card, int position);
    }

    public MatchingAdapter(ArrayList<MatchingCard> cards, OnCardClickListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_matching_card, parent, false);
        return new MatchingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchingViewHolder holder, int position) {
        MatchingCard card = cards.get(position);
        
        if (card.isMatched()) {
            holder.cardText.setText(card.getText());
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.matched_card));
        } else if (card.isFlipped()) {
            holder.cardText.setText(card.getText());
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.flipped_card));
        } else {
            holder.cardText.setText("?");
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.hidden_card));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClick(card, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class MatchingViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView cardText;

        public MatchingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.matchingCardView);
            cardText = itemView.findViewById(R.id.matchingCardText);
        }
    }
}

