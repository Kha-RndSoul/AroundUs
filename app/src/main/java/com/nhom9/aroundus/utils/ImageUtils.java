package com.nhom9.aroundus.utils;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

public class ImageUtils {

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadListener listener) {
        MediaManager.get().upload(imageUri)
                .option("folder", "aroundus/places")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, java.util.Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        listener.onSuccess(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        listener.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch(context);
    }
}