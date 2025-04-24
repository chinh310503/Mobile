package com.example.myapplication.DAO;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myapplication.Model.CafeModel;
import com.example.myapplication.Session.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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


    public void saveSearchHistory(String keyword) {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        // Đếm số lượng document hiện có để lấy id mới
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

    public void getAllCafes(double userLat, double userLon, CafeListCallback callback) {
        db.collection("cafes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CafeModel> cafeList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CafeModel cafe = parseCafe(doc, userLat, userLon);
                        if (cafe != null) {
                            cafeList.add(cafe);
                        }
                    }
                    callback.onResult(cafeList);
                })
                .addOnFailureListener(callback::onError);
    }

    // Parse 1 document từ Firestore thành CafeModel
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

            double distance = calculateDistance(userLat, userLon, geo.getLatitude(), geo.getLongitude());
            cafe.setDistance(Math.round(distance * 10.0) / 10.0);

            return cafe;
        } catch (Exception e) {
            Log.e("ParseCafe", "Lỗi khi parse quán cafe: " + e.getMessage());
            return null;
        }
    }

    // 👉 Tính khoảng cách giữa 2 điểm (Haversine formula)
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
