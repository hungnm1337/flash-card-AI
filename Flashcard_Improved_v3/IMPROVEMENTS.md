# Flashcard App - Advanced Learning Features

## Overview
This enhanced version of the Flashcard app includes advanced learning features to improve the study experience.

## New Features Added

### 1. Spaced Repetition System
- **Algorithm**: Implemented SM-2 spaced repetition algorithm
- **Database**: Added new columns to track review intervals, repetitions, ease factor
- **UI**: Updated CardFlip with 4-button rating system (Again, Hard, Good, Easy)
- **Smart Scheduling**: Only shows cards that are due for review

### 2. Quiz Modes
- **Multiple Choice Quiz**: 4-option quiz with randomized answers
- **Matching Game**: Memory-style card matching game
- **Adaptive**: Requires minimum number of words to function

### 3. Text-to-Speech Features
- **Listen & Learn**: Hear pronunciation of words and meanings
- **Speed Control**: Adjustable speech rate (0.5x to 2.0x)
- **Listening Quiz**: Type what you hear challenge
- **Language Support**: Uses device's TTS engine

### 4. Study Mode Selection
- **Unified Interface**: StudyModesActivity provides access to all learning modes
- **Mode Cards**: Visual selection of different study approaches
- **Seamless Navigation**: Easy switching between modes

## Technical Improvements

### Database Migration
- Safe upgrade from version 1 to 2
- Preserves existing data
- Adds spaced repetition columns with default values

### Code Structure
- Modular design with separate packages for each feature
- Backward compatibility maintained
- Clean separation of concerns

## File Structure
```
app/src/main/java/com/example/flashcard/
├── SpacedRepetitionAlgorithm.java
├── ui/
│   ├── modes/StudyModesActivity.java
│   ├── quiz/
│   │   ├── QuizActivity.java
│   │   ├── MatchingGameActivity.java
│   │   ├── MatchingCard.java
│   │   └── MatchingAdapter.java
│   └── tts/
│       ├── TTSActivity.java
│       └── ListeningQuizActivity.java
├── modal/WordModel.java (enhanced)
└── db/DBHandler.java (enhanced)
```

## Usage Instructions

1. **Spaced Repetition**: Use the enhanced flashcard mode with 4 difficulty buttons
2. **Quiz Mode**: Test knowledge with multiple choice questions
3. **Matching Game**: Play memory game to reinforce learning
4. **Listen & Learn**: Practice pronunciation and listening skills
5. **Listening Quiz**: Challenge listening comprehension

## Requirements
- Android device with TTS support
- Minimum 3-4 words in folder for quiz modes
- Audio output for TTS features

## Future Enhancements
- Import/Export functionality
- Cloud synchronization
- Statistics and progress tracking
- Custom themes and UI improvements

