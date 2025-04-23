package com.example.myapplication.Session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_IMG = "user_img";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUserSession(int id, String email, String name, String img) {
        editor.putString(KEY_EMAIL, email);
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_IMG, img);
        editor.apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_ID, -1);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_NAME, null);
    }

    public String getUserImg() {
        return sharedPreferences.getString(KEY_IMG, null);
    }

    public boolean isLoggedIn() {
        return getUserEmail() != null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
