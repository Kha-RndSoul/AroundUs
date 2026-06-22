package com.nhom9.aroundus.utils;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

public class ImageUtils {

    // Đổi theo preset bạn đã tạo trên Cloudinary
    private static final String UPLOAD_PRESET = "aroundus_unsigned_preset";
    private static final String FOLDER = "aroundus/places";

    // 2MB để tiết kiệm quota free
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    /**
     * Upload ảnh lên Cloudinary bằng unsigned preset.
     */
    public static void uploadImage(Context context, Uri imageUri, UploadListener listener) {
        if (imageUri == null) {
            listener.onError("Ảnh không hợp lệ");
            return;
        }

        MediaManager.get().upload(imageUri)
                .option("upload_preset", UPLOAD_PRESET)
                .option("folder", FOLDER)
                .option("resource_type", "image")
                .option("allowed_formats", "jpg,jpeg,png,webp")
                .option("max_file_size", MAX_FILE_SIZE)

                // Tối ưu dung lượng ảnh để tiết kiệm quota:
                // - Giới hạn chiều lớn nhất 1280
                // - quality auto
                // - format auto
                .option("transformation", "c_limit,w_1280,h_1280,q_auto,f_auto")

                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // optional
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // optional
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Object secureUrl = resultData.get("secure_url");
                        if (secureUrl != null) {
                            listener.onSuccess(secureUrl.toString());
                        } else {
                            listener.onError("Không lấy được URL ảnh");
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        String msg = (error != null && error.getDescription() != null)
                                ? error.getDescription()
                                : "Upload thất bại";
                        listener.onError(msg);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // optional
                    }
                })
                .dispatch(context);
    }
}