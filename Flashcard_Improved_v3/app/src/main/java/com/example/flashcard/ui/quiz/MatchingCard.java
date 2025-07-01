package com.example.flashcard.ui.quiz;

public class MatchingCard {
    public enum CardType {
        WORD, MEANING
    }
    
    private String text;
    private CardType type;
    private int wordId;
    private boolean isFlipped;
    private boolean isMatched;

    public MatchingCard(String text, CardType type, int wordId) {
        this.text = text;
        this.type = type;
        this.wordId = wordId;
        this.isFlipped = false;
        this.isMatched = false;
    }

    public String getText() { return text; }
    public CardType getType() { return type; }
    public int getWordId() { return wordId; }
    public boolean isFlipped() { return isFlipped; }
    public boolean isMatched() { return isMatched; }

    public void setFlipped(boolean flipped) { this.isFlipped = flipped; }
    public void setMatched(boolean matched) { this.isMatched = matched; }
}

