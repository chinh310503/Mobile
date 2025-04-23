package com.example.myapplication.DAO;

import android.util.Log;

import com.example.myapplication.Model.CafeModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CafeSearchDAO {
    private final FirebaseFirestore db;

    public CafeSearchDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public interface CafeListCallback {
        void onResult(List<CafeModel> cafes);
        void onError(Exception e);
    }

    public void searchCafesByName(String keyword, double userLat, double userLon, CafeListCallback callback) {
        db.collection("cafes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CafeModel> cafeList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CafeModel cafe = parseCafe(doc, userLat, userLon);
                        if (cafe != null && cafe.getName().toLowerCase().contains(keyword.toLowerCase())) {
                            cafeList.add(cafe);
                        }
                    }
                    callback.onResult(cafeList);
                })
                .addOnFailureListener(callback::onError);
    }

    public void filterCafes(boolean requireWifi, boolean requireWorkspace, boolean requireOpenNow,
                            double userLat, double userLon, CafeListCallback callback) {
        db.collection("cafes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CafeModel> filteredList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CafeModel cafe = parseCafe(doc, userLat, userLon);
                        if (cafe == null) continue;

                        boolean match = (!requireWifi || cafe.isWifiAvailable())
                                && (!requireWorkspace || cafe.isWorkSpace())
                                && (!requireOpenNow || cafe.isOpen());

                        if (match) {
                            filteredList.add(cafe);
                        }
                    }
                    callback.onResult(filteredList);
                })
                .addOnFailureListener(callback::onError);
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

    // 👉 Parse 1 document từ Firestore thành CafeModel
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
