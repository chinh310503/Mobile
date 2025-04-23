package com.example.myapplication.DataBase;

import com.google.firebase.firestore.FirebaseFirestore;

public class DBHandler {
    private static DBHandler instance;
    private final FirebaseFirestore db;

    private DBHandler() {
        db = FirebaseFirestore.getInstance(); // ⚠️ Sử dụng đúng tên
    }

    public static synchronized DBHandler getInstance() {
        if (instance == null) {
            instance = new DBHandler();
        }
        return instance;
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
