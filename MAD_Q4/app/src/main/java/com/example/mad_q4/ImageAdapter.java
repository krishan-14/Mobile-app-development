package com.example.photogallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;

/**
 * ImageAdapter — populates the 3-column RecyclerView grid in GalleryActivity.
 *
 * Key behaviour:
 *   - Each cell is made perfectly SQUARE at runtime (screen_width / 3 columns).
 *   - Glide loads thumbnails with disk caching so scrolling is smooth.
 *   - Tapping any cell fires the OnImageClickListener with the image's absolute path.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    // ---------------------------------------------------------------
    // Callback interface
    // ---------------------------------------------------------------

    /** Called when the user taps an image in the grid. */
    public interface OnImageClickListener {
        void onImageClick(String imagePath);
    }

    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------
    private final Context              context;
    private final List<File>           imageFiles;
    private final OnImageClickListener listener;

    /** Calculated once: pixel size of each square cell. */
    private int cellSize = 0;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    public ImageAdapter(Context context,
                        List<File> imageFiles,
                        OnImageClickListener listener) {
        this.context    = context;
        this.imageFiles = imageFiles;
        this.listener   = listener;
    }

    // ---------------------------------------------------------------
    // RecyclerView.Adapter overrides
    // ---------------------------------------------------------------

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Calculate square cell size once (screen width / 3 columns - padding gaps)
        if (cellSize == 0) {
            float density   = context.getResources().getDisplayMetrics().density;
            int   padding   = (int) (4 * density); // 4dp per gap
            int   width     = parent.getMeasuredWidth();
            cellSize = (width - padding * 4) / 3;
            if (cellSize <= 0) cellSize = (int)(120 * density); // safe fallback
        }

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_image, parent, false);

        // Force square height = calculated width
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = cellSize;
        view.setLayoutParams(lp);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File imageFile = imageFiles.get(position);

        // Load thumbnail with Glide:
        //   centerCrop        - fills square without distortion
        //   thumbnail(0.1f)   - loads a tiny preview first for a snappy feel
        //   DiskCacheStrategy - caches decoded bitmaps for fast re-scroll
        Glide.with(context)
                .load(imageFile)
                .centerCrop()
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error)
                .into(holder.imageView);

        // Fire click callback with absolute path
        holder.imageView.setOnClickListener(v ->
                listener.onImageClick(imageFile.getAbsolutePath()));
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    // ---------------------------------------------------------------
    // ViewHolder
    // ---------------------------------------------------------------

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivGridImage);
        }
    }
}