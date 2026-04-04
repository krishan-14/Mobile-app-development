package com.example.photogallery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * MainActivity — Home screen of the Photo Gallery app.
 *
 * Responsibilities:
 *   1. Request runtime permissions (CAMERA + storage).
 *   2. Launch the device camera; save the captured photo to a folder.
 *   3. Let the user pick a folder and open GalleryActivity.
 */
public class MainActivity extends AppCompatActivity {

    // ---------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Path of the photo file being taken (stored so we can pass it after capture)
    private String currentPhotoPath;

    // ---------------------------------------------------------------
    // Activity-result launchers (modern replacement for onActivityResult)
    // ---------------------------------------------------------------

    /**
     * Launched when the user presses "Take Photo".
     * If the camera returns RESULT_OK the photo is already saved at currentPhotoPath.
     */
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Toast.makeText(this,
                                    "Photo saved to: " + currentPhotoPath,
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    /**
     * Launched when the user presses "Choose Folder".
     * Opens the system's document/folder picker; we get back the folder URI.
     */
    private final ActivityResultLauncher<Uri> folderPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocumentTree(),
                    uri -> {
                        if (uri != null) {
                            // Convert content URI → real path string for display
                            String folderPath = UriUtils.getPathFromUri(this, uri);

                            // Open GalleryActivity with the selected folder path
                            Intent intent = new Intent(this, GalleryActivity.class);
                            intent.putExtra(GalleryActivity.EXTRA_FOLDER_PATH, folderPath);
                            startActivity(intent);
                        }
                    });

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions on first launch
        requestPermissions();

        // --- Take Photo button ---
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(v -> {
            if (hasPermissions()) {
                launchCamera();
            } else {
                requestPermissions();
                Toast.makeText(this, "Please grant permissions first.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Choose Folder button ---
        Button btnChooseFolder = findViewById(R.id.btnChooseFolder);
        btnChooseFolder.setOnClickListener(v -> {
            if (hasPermissions()) {
                // Open system folder picker
                folderPickerLauncher.launch(null);
            } else {
                requestPermissions();
                Toast.makeText(this, "Please grant permissions first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------------
    // Camera helpers
    // ---------------------------------------------------------------

    /**
     * Creates a unique image file and fires the camera intent.
     * The photo is saved to getExternalFilesDir(Pictures) — no WRITE permission needed on API 29+.
     */
    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.photogallery.fileprovider",
                    photoFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(cameraIntent);

        } catch (IOException e) {
            Toast.makeText(this, "Could not create image file: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates an empty JPEG file with a timestamp name inside the Pictures folder.
     *
     * @return The new (empty) File object.
     * @throws IOException if the file cannot be created.
     */
    private File createImageFile() throws IOException {
        // Unique filename: IMG_20240403_143022.jpg
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        // getExternalFilesDir creates the directory if it doesn't exist
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save the path so we can display it after capture
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // ---------------------------------------------------------------
    // Permission helpers
    // ---------------------------------------------------------------

    /** Returns true only when all required permissions are granted. */
    private boolean hasPermissions() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            return checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 and below uses READ_EXTERNAL_STORAGE
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /** Asks the user to grant all required permissions at once. */
    private void requestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this,
                        "Permissions are required for this app to work.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}