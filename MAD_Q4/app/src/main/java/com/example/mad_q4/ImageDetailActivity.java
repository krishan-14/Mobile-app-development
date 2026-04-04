package com.example.photogallery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ImageDetailActivity — shows full metadata for a single image and allows deletion.
 *
 * Receives:
 *   EXTRA_IMAGE_PATH  (String) — absolute path of the image to display.
 *
 * Flow:
 *   1. Load & display the image with Glide.
 *   2. Show: name, full path, file size, date taken.
 *   3. "Delete" button → confirmation AlertDialog → delete file → finish() (returns to gallery).
 */
public class ImageDetailActivity extends AppCompatActivity {

    /** Intent extra key for the image path. */
    public static final String EXTRA_IMAGE_PATH = "extra_image_path";

    // ---------------------------------------------------------------
    // Views
    // ---------------------------------------------------------------
    private ImageView ivDetail;
    private TextView  tvName;
    private TextView  tvPath;
    private TextView  tvSize;
    private TextView  tvDate;
    private Button    btnDelete;

    // ---------------------------------------------------------------
    // Data
    // ---------------------------------------------------------------
    private File imageFile;

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Image Details");
        }

        // --- Bind views ---
        ivDetail  = findViewById(R.id.ivDetail);
        tvName    = findViewById(R.id.tvName);
        tvPath    = findViewById(R.id.tvPath);
        tvSize    = findViewById(R.id.tvSize);
        tvDate    = findViewById(R.id.tvDate);
        btnDelete = findViewById(R.id.btnDelete);

        // --- Get image path ---
        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "Image path not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Toast.makeText(this, "Image file does not exist.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Populate UI ---
        displayImageDetails();

        // --- Delete button ---
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // ---------------------------------------------------------------
    // UI helpers
    // ---------------------------------------------------------------

    /**
     * Loads the image with Glide and fills all metadata TextViews.
     */
    private void displayImageDetails() {
        // Full image preview
        Glide.with(this)
                .load(imageFile)
                .fitCenter()
                .into(ivDetail);

        // Name
        tvName.setText(imageFile.getName());

        // Full path
        tvPath.setText(imageFile.getAbsolutePath());

        // Human-readable file size (B / KB / MB)
        tvSize.setText(formatFileSize(imageFile.length()));

        // Date taken = file last-modified date (EXIF reading is out of scope)
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String dateTaken = sdf.format(new Date(imageFile.lastModified()));
        tvDate.setText(dateTaken);
    }

    /**
     * Shows an AlertDialog asking the user to confirm the deletion.
     * On confirmation: deletes the file, shows a toast, and calls finish()
     * so the user returns to GalleryActivity (which refreshes on onResume).
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image?")
                .setMessage("This will permanently delete \""
                        + imageFile.getName()
                        + "\" from your device.\n\nThis action cannot be undone.")
                // Destructive action button
                .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                // Cancel — dismiss dialog, stay on details screen
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    /**
     * Deletes the image file from the device.
     * Navigates back to GalleryActivity on success.
     */
    private void deleteImage() {
        if (imageFile.delete()) {
            Toast.makeText(this, "Image deleted successfully.", Toast.LENGTH_SHORT).show();
            finish(); // Return to GalleryActivity → onResume() refreshes the grid
        } else {
            Toast.makeText(this, "Failed to delete image. Check permissions.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------------------------------------------------
    // Static utility
    // ---------------------------------------------------------------

    /**
     * Converts raw byte count to a readable string: "1.23 MB", "456 KB", "789 B".
     *
     * @param bytes File size in bytes.
     * @return Formatted string.
     */
    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        DecimalFormat df = new DecimalFormat("#.##");
        if (bytes < 1024)          return bytes + " B";
        if (bytes < 1024 * 1024)   return df.format(bytes / 1024.0) + " KB";
        return df.format(bytes / (1024.0 * 1024.0)) + " MB";
    }
}