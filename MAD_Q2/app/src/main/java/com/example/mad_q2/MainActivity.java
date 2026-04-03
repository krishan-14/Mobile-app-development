package com.example.mad_q2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Subject: Mobile Application Development (CSE3709)
 * Question 2: Media Player with Audio (Disk) and Video (URL/Disk)
 */
public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private VideoView videoView;
    private TextView audioStatus;
    private static final String TAG = "MediaPlayerLog";

    // Launcher for Audio Picking
    private final ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri audioUri = result.getData().getData();
                    if (audioUri != null) {
                        String fileName = audioUri.getLastPathSegment();
                        audioStatus.setText(getString(R.string.status_loaded, fileName));
                        setupAudioPlayer(audioUri);
                    }
                }
            }
    );

    // Launcher for Video Picking
    private final ActivityResultLauncher<Intent> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri videoUri = result.getData().getData();
                    if (videoUri != null) {
                        videoView.setVideoURI(videoUri);
                        videoView.start();
                        Toast.makeText(this, "Playing local video", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioStatus = findViewById(R.id.audioStatus);
        videoView = findViewById(R.id.videoView);

        // Setup MediaController for VideoView
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Optional: Ensure video starts playing when prepared
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            Log.d(TAG, "Video is ready to play");
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Video Error: " + what + ", " + extra);
            Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show();
            return true;
        });

        // --- Audio Logic ---
        findViewById(R.id.btnOpenFile).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            audioPickerLauncher.launch(intent);
        });

        findViewById(R.id.btnPlayAudio).setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                audioStatus.setText(getString(R.string.status_playing));
            } else {
                Toast.makeText(this, "Please select an audio file first", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnPauseAudio).setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                audioStatus.setText(getString(R.string.status_paused));
            }
        });

        findViewById(R.id.btnStopAudio).setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                try {
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    Log.e(TAG, "Error stopping audio", e);
                }
                audioStatus.setText(getString(R.string.status_stopped));
            }
        });

        // --- Video Logic ---
        findViewById(R.id.btnOpenUrl).setOnClickListener(v -> {
            String videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4";
            videoView.setVideoPath(videoUrl);
            videoView.start();
            Toast.makeText(this, "Loading Stream...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnSelectVideo).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            videoPickerLauncher.launch(intent);
        });

        findViewById(R.id.btnRestartVideo).setOnClickListener(v -> {
            videoView.seekTo(0);
            videoView.start();
        });
    }

    private void setupAudioPlayer(Uri uri) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, uri);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            audioStatus.setText(getString(R.string.status_playing));
        } else {
            Toast.makeText(this, "Failed to load audio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
