package com.example.flashcard.ui.media;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MediaWordFormActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_AUDIO_PICK = 1002;
    private static final int REQUEST_PERMISSIONS = 1003;

    private int wordId;
    private String currentImagePath;
    private String currentAudioPath;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private boolean isPlaying = false;

    private ImageView imagePreview;
    private TextView audioStatus;
    private Button selectImageBtn;
    private Button selectAudioBtn;
    private Button recordAudioBtn;
    private Button playAudioBtn;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_word_form);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Add Media to Word");

        wordId = getIntent().getIntExtra("word_id", -1);
        if (wordId == -1) {
            Toast.makeText(this, "Invalid word ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        checkPermissions();
    }

    private void initViews() {
        imagePreview = findViewById(R.id.imagePreview);
        audioStatus = findViewById(R.id.audioStatus);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        selectAudioBtn = findViewById(R.id.selectAudioBtn);
        recordAudioBtn = findViewById(R.id.recordAudioBtn);
        playAudioBtn = findViewById(R.id.playAudioBtn);
        saveBtn = findViewById(R.id.saveBtn);

        playAudioBtn.setEnabled(false);
    }

    private void setupListeners() {
        selectImageBtn.setOnClickListener(v -> selectImage());
        selectAudioBtn.setOnClickListener(v -> selectAudio());
        recordAudioBtn.setOnClickListener(v -> toggleRecording());
        playAudioBtn.setOnClickListener(v -> togglePlayback());
        saveBtn.setOnClickListener(v -> saveMedia());
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            };
        }

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void selectAudio() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_AUDIO_PICK);
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void startRecording() {
        if (!hasStoragePermission()) {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File audioFile;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "word_" + wordId + "_" + System.currentTimeMillis() + ".3gp");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);
                Uri audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri audioUri = getContentResolver().insert(audioCollection, values);
                currentAudioPath = audioUri.toString();
            } else {
                audioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), 
                    "word_" + wordId + "_" + System.currentTimeMillis() + ".3gp");
                currentAudioPath = audioFile.getAbsolutePath();
            }

            if (currentAudioPath == null) {
                Toast.makeText(this, "Audio path is null", Toast.LENGTH_SHORT).show();
                return;
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaRecorder.setOutputFile(getContentResolver().openFileDescriptor(Uri.parse(currentAudioPath), "w").getFileDescriptor());
            } else {
                mediaRecorder.setOutputFile(currentAudioPath);
            }

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            recordAudioBtn.setText("Stop Recording");
            audioStatus.setText("Recording...");
        } catch (IOException e) {
            Log.e("MediaWordFormActivity", "Failed to start recording", e);
            Toast.makeText(this, "Failed to start recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        isRecording = false;
        recordAudioBtn.setText("Record Audio");
        audioStatus.setText("Audio recorded");
        playAudioBtn.setEnabled(true);
    }

    private void togglePlayback() {
        if (!isPlaying) {
            startPlayback();
        } else {
            stopPlayback();
        }
    }

    private void startPlayback() {
        if (currentAudioPath == null) {
            Toast.makeText(this, "Audio path is null", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaPlayer.setDataSource(this, Uri.parse(currentAudioPath));
            } else {
                mediaPlayer.setDataSource(currentAudioPath);
            }
            mediaPlayer.prepare();
            mediaPlayer.start();

            isPlaying = true;
            playAudioBtn.setText("Stop Playing");

            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlayback();
            });
        } catch (IOException e) {
            Log.e("MediaWordFormActivity", "Failed to play audio", e);
            Toast.makeText(this, "Failed to play audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        isPlaying = false;
        playAudioBtn.setText("Play Audio");
    }

    private void saveMedia() {
        DBHandler dbHandler = new DBHandler(this);
        dbHandler.updateWordMedia(wordId, currentImagePath, currentAudioPath);
        
        Toast.makeText(this, "Media saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();

            if (requestCode == REQUEST_IMAGE_PICK) {
                handleImageSelection(selectedUri);
            } else if (requestCode == REQUEST_AUDIO_PICK) {
                handleAudioSelection(selectedUri);
            }
        }
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Save image to app's private storage using MediaStore for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "word_" + wordId + "_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri newImageUri = getContentResolver().insert(imageCollection, values);

                try (OutputStream outputStream = getContentResolver().openOutputStream(newImageUri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
                currentImagePath = newImageUri.toString();
            } else {
                File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), 
                    "word_" + wordId + "_" + System.currentTimeMillis() + ".jpg");
                
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.close();
                
                currentImagePath = imageFile.getAbsolutePath();
            }
            imagePreview.setImageBitmap(bitmap);
            imagePreview.setVisibility(View.VISIBLE);
            
        } catch (IOException e) {
            Log.e("MediaWordFormActivity", "Failed to load image", e);
            Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAudioSelection(Uri audioUri) {
        try {
            // Copy audio file to app's private storage using MediaStore for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "word_" + wordId + "_" + System.currentTimeMillis() + ".mp3");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);
                Uri audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri newAudioUri = getContentResolver().insert(audioCollection, values);

                try (InputStream inputStream = getContentResolver().openInputStream(audioUri);
                     OutputStream outputStream = getContentResolver().openOutputStream(newAudioUri)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
                currentAudioPath = newAudioUri.toString();
            } else {
                InputStream inputStream = getContentResolver().openInputStream(audioUri);
                File audioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), 
                    "word_" + wordId + "_" + System.currentTimeMillis() + ".mp3");
                
                FileOutputStream outputStream = new FileOutputStream(audioFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
                
                currentAudioPath = audioFile.getAbsolutePath();
            }
            audioStatus.setText("Audio file selected");
            playAudioBtn.setEnabled(true);
            
        } catch (IOException e) {
            Log.e("MediaWordFormActivity", "Failed to load audio", e);
            Toast.makeText(this, "Failed to load audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}


