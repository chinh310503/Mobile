package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.Cloudinary.CloudinaryHelper;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CloudinaryHelper.init(this);  // Khởi tạo Cloudinary một lần duy nhất tại đây
    }
}
