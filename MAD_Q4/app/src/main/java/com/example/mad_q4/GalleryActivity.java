package com.example.photogallery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GalleryActivity — displays all images inside a chosen folder in a 3-column grid.
 *
 * Receives:
 *   EXTRA_FOLDER_PATH  (String) — absolute path of the folder to display.
 *
 * On image tap → opens ImageDetailActivity with the image path.
 */
public class GalleryActivity extends AppCompatActivity {

    /** Intent extra key for the folder path. */
    public static final String EXTRA_FOLDER_PATH = "extra_folder_path";

    // ---------------------------------------------------------------
    // Views
    // ---------------------------------------------------------------
    private RecyclerView recyclerView;
    private TextView    tvFolderName;
    private TextView    tvImageCount;

    // ---------------------------------------------------------------
    // Data
    // ---------------------------------------------------------------
    private ImageAdapter    adapter;
    private List<File>      imageFiles = new ArrayList<>();
    private String          folderPath;

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // --- Bind views ---
        recyclerView  = findViewById(R.id.recyclerViewGallery);
        tvFolderName  = findViewById(R.id.tvFolderName);
        tvImageCount  = findViewById(R.id.tvImageCount);

        // Back arrow in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gallery");
        }

        // --- Get folder path from intent ---
        folderPath = getIntent().getStringExtra(EXTRA_FOLDER_PATH);
        if (folderPath == null || folderPath.isEmpty()) {
            Toast.makeText(this, "No folder path received.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show folder name in UI
        File folder = new File(folderPath);
        tvFolderName.setText(folder.getName());

        // --- Setup RecyclerView with 3-column grid ---
        adapter = new ImageAdapter(this, imageFiles, imagePath -> {
            // On image click → open ImageDetailActivity
            Intent intent = new Intent(GalleryActivity.this, ImageDetailActivity.class);
            intent.putExtra(ImageDetailActivity.EXTRA_IMAGE_PATH, imagePath);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        // --- Load images ---
        loadImages(folder);
    }

    /**
     * Called when returning from ImageDetailActivity (e.g. after a delete).
     * Refreshes the grid so deleted images disappear.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Re-scan folder on every resume to pick up deletions
        if (folderPath != null) {
            loadImages(new File(folderPath));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Scans the given folder for image files (jpg, jpeg, png, gif, webp)
     * and refreshes the RecyclerView adapter.
     *
     * @param folder The directory to scan.
     */
    private void loadImages(File folder) {
        imageFiles.clear();

        if (!folder.exists() || !folder.isDirectory()) {
            Toast.makeText(this, "Folder not found.", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            tvImageCount.setText("0 images");
            return;
        }

        // Filter only image files by extension
        File[] files = folder.listFiles(file ->
                file.isFile() && isImageFile(file.getName()));

        if (files != null && files.length > 0) {
            // Sort by last-modified descending (newest first)
            Arrays.sort(files, (a, b) ->
                    Long.compare(b.lastModified(), a.lastModified()));

            imageFiles.addAll(Arrays.asList(files));
        }

        adapter.notifyDataSetChanged();
        tvImageCount.setText(imageFiles.size() + " image" +
                (imageFiles.size() == 1 ? "" : "s"));
    }

    /**
     * Returns true if the filename has a common image extension.
     *
     * @param name Filename to check.
     * @return true if it is an image file.
     */
    private boolean isImageFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp");
    }
}