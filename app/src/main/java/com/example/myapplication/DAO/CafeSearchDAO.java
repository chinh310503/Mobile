package com.example.myapplication.DAO;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.example.myapplication.Model.CafeModel;
import com.example.myapplication.Session.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CafeSearchDAO {
    private final FirebaseFirestore db;
    private final SessionManager sessionManager;
    public CafeSearchDAO(Context context) {
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(context);
    }

    public interface CafeListCallback {
        void onResult(List<CafeModel> cafes);
        void onError(Exception e);
    }

    public interface SearchHistoryCallback {
        void onResult(List<String> history);
        void onError(Exception e);
    }

    public void getSearchHistory(SearchHistoryCallback callback) {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            callback.onError(new Exception("Người dùng chưa đăng nhập hoặc không tìm thấy ID"));
            return;
        }

        db.collection("search_history")
                .whereEqualTo("id_user", userId)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> result = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String keyword = doc.getString("search_query");
                        if (keyword != null) result.add(keyword);
                    }
                    callback.onResult(result);
                })
                .addOnFailureListener(callback::onError);
    }

    public void clearSearchHistory(SearchHistoryCallback callback) {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            callback.onError(new Exception("Người dùng chưa đăng nhập hoặc không tìm thấy ID"));
            return;
        }

        db.collection("search_history")
                .whereEqualTo("id_user", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onResult(new ArrayList<>()))
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }



    public void saveSearchHistory(String keyword) {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        db.collection("search_history")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int newId = queryDocumentSnapshots.size() + 1;

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", newId);
                    data.put("id_user", userId);
                    data.put("search_query", keyword);

                    db.collection("search_history").add(data)
                            .addOnSuccessListener(documentReference -> Log.d("SearchDAO", "Saved search history with ID: " + newId))
                            .addOnFailureListener(e -> Log.e("SearchDAO", "Failed to save search history", e));
                })
                .addOnFailureListener(e -> Log.e("SearchDAO", "Failed to count existing histories", e));
    }

    public void getAllCafes(CafeListCallback callback) {
        db.collection("cafes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CafeModel> cafeList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CafeModel cafe = parseCafe(doc, sessionManager.getUserLatitude(), sessionManager.getUserLongitude());
                        if (cafe != null) {
                            cafeList.add(cafe);
                        }
                    }
                    callback.onResult(cafeList);
                })
                .addOnFailureListener(callback::onError);
    }

    private CafeModel parseCafe(DocumentSnapshot doc, double userLat, double userLon) {
        try {
            GeoPoint geo = doc.getGeoPoint("geopoint");
            if (geo == null) return null;

            CafeModel cafe = new CafeModel();
            cafe.setId(doc.getLong("id").intValue());
            cafe.setName(doc.getString("name"));
            cafe.setLat(geo.getLatitude());
            cafe.setLon(geo.getLongitude());
            cafe.setAddress(doc.getString("address"));
            cafe.setWifiAvailable(Boolean.TRUE.equals(doc.getBoolean("wifi_available")));
            cafe.setWorkSpace(Boolean.TRUE.equals(doc.getBoolean("work_space")));
            cafe.setPhone(doc.getString("phone"));
            cafe.setMinPrice(doc.getDouble("min_price"));
            cafe.setImg(doc.getString("img"));
            cafe.setDescription(doc.getString("description"));
            cafe.setRating(doc.getLong("rating") != null ? doc.getLong("rating") : 0);
            cafe.setTotalRating(doc.getLong("total_rating") != null ? doc.getLong("total_rating") : 0);
            cafe.setOpenHours(doc.getString("open_hours"));
            cafe.setCloseHours(doc.getString("close_hours"));

            Location currentLocation = new Location("");
            currentLocation.setLatitude(userLat);
            currentLocation.setLongitude(userLon);

            Location cafeLocation = new Location("");
            cafeLocation.setLatitude(geo.getLatitude());
            cafeLocation.setLongitude(geo.getLongitude());
            double distance = currentLocation.distanceTo(cafeLocation) / 1000;
            cafe.setDistance(Math.round(distance * 10.0) / 10.0);
            return cafe;
        } catch (Exception e) {
            Log.e("ParseCafe", "Lỗi khi parse quán cafe: " + e.getMessage());
            return null;
        }
    }
}
