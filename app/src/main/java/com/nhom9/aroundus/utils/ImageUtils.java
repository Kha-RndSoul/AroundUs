package com.nhom9.aroundus.utils;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

public class ImageUtils {

    private static final String UPLOAD_PRESET = "aroundus_unsigned_preset";

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadListener listener) {
        if (imageUri == null) {
            listener.onError("Ảnh không hợp lệ");
            return;
        }

        MediaManager.get().upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Object secureUrl = resultData.get("secure_url");
                        if (secureUrl != null) listener.onSuccess(secureUrl.toString());
                        else listener.onError("Không lấy được URL ảnh");
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        listener.onError(error != null ? error.getDescription() : "Upload thất bại");
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }
}