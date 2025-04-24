package com.example.myapplication.DAO;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.Session.SessionManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FavoriteCafeDAO {
    private final FirebaseFirestore db;
    private final CollectionReference favRef;
    private final SessionManager sessionManager;

    public interface FavoriteCallback {
        void onSuccess(boolean isFavorite);
        void onFailure(Exception e);
    }

    public FavoriteCafeDAO(Context context) {
        db = FirebaseFirestore.getInstance();
        favRef = db.collection("favorite_cafe");
        sessionManager = new SessionManager(context);
    }

    public void addFavorite(long cafeId) {
        int userId = sessionManager.getUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("id_user", userId);
        data.put("id_cafe", cafeId);
        favRef.add(data)
                .addOnSuccessListener(doc -> Log.d("FavoriteDAO", "Đã thêm yêu thích"))
                .addOnFailureListener(e -> Log.e("FavoriteDAO", "Lỗi khi thêm", e));
    }

    public void removeFavorite(long cafeId) {
        int userId = sessionManager.getUserId();
        favRef.whereEqualTo("id_user", userId)
                .whereEqualTo("id_cafe", cafeId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (var doc : snapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    Log.d("FavoriteDAO", "Đã xoá khỏi yêu thích");
                })
                .addOnFailureListener(e -> Log.e("FavoriteDAO", "Lỗi khi xoá", e));
    }

    public interface FavoriteListCallback {
        void onSuccess(Set<Long> favoriteIds);
        void onFailure(Exception e);
    }

    public void getFavoritesByUserId(FavoriteListCallback callback) {
        int userId = sessionManager.getUserId();
        favRef.whereEqualTo("id_user", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<Long> favoriteIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Long idCafe = doc.getLong("id_cafe");
                        if (idCafe != null) favoriteIds.add(idCafe);
                    }
                    callback.onSuccess(favoriteIds);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
