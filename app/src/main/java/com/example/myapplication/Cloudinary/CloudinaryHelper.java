package com.example.myapplication.Cloudinary;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {

    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "drqkphhmi");
            config.put("api_key", "839415397899152");
            config.put("api_secret", "YsV0AC8MmjlE43hkLUVvWySwLmo");

            MediaManager.init(context, config);
            isInitialized = true;
        }
    }

    public interface UploadImageCallback {
        void onComplete(String url);
    }

    public static void uploadImage(Uri uri, Context context, UploadImageCallback callback) {
        init(context); // đảm bảo đã khởi tạo MediaManager

        MediaManager.get().upload(uri)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}

                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        callback.onComplete(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onComplete(null);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onComplete(null);
                    }
                })
                .dispatch();
    }
}
