package com.example.photogallery;

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.os.Environment;

/**
 * UriUtils — utility to convert a folder content:// URI (from OpenDocumentTree)
 * to a real filesystem path string that File() can use.
 *
 * OpenDocumentTree returns URIs like:
 *   content://com.android.externalstorage.documents/tree/primary%3ADCIM%2FCamera
 *
 * We parse the document ID (e.g. "primary:DCIM/Camera") and build the real path.
 */
public class UriUtils {

    /**
     * Converts an OpenDocumentTree URI to an absolute file-system path.
     *
     * Supports:
     *   - Primary external storage  (primary:DCIM/Camera)
     *   - SD card / secondary       (XXXX-XXXX:DCIM/Camera)
     *
     * Falls back to the URI's string representation if conversion is not possible.
     *
     * @param context Application context.
     * @param uri     URI returned by OpenDocumentTree.
     * @return Absolute path string, e.g. "/storage/emulated/0/DCIM/Camera".
     */
    public static String getPathFromUri(Context context, Uri uri) {
        // Get the document ID from the tree URI
        // e.g. "primary:DCIM/Camera"
        String docId = DocumentsContract.getTreeDocumentId(uri);

        if (docId.startsWith("primary:")) {
            // Primary (internal) external storage
            String relativePath = docId.replace("primary:", "");
            return Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + relativePath;

        } else if (docId.contains(":")) {
            // Secondary storage / SD card — volume ID before the colon
            String[] parts = docId.split(":");
            String volumeId   = parts[0]; // e.g. "ABCD-1234"
            String relPath    = parts.length > 1 ? parts[1] : "";

            // Standard Android path for secondary storage
            return "/storage/" + volumeId + "/" + relPath;
        }

        // Fallback: use the raw URI (won't work with File() but prevents crash)
        return uri.toString();
    }
}