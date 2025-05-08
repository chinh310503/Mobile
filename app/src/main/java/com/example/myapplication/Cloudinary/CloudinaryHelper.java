package com.example.myapplication.Cloudinary;
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//
//public class CloudinaryHelper {
//    private Cloudinary mCloudinary;
//
//    public CloudinaryHelper() {
//        // Cấu hình Cloudinary với API key, secret và cloud name
//        mCloudinary = new Cloudinary(ObjectUtils.asMap(
//                "cloud_name", "drqkphhmi",
//                "api_key", "839415397899152",
//                "api_secret", "YsV0AC8MmjlE43hkLUVvWySwLmo"));
//    }
//
//    public Cloudinary getCloudinaryInstance() {
//        return mCloudinary;
//    }
//}

import android.content.Context;

import com.cloudinary.android.MediaManager;

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
}