package com.example.myapplication.Model;

import java.io.Serializable;
import java.util.Calendar;

public class CafeModel implements Serializable {
    private long id;
    private String name;
    private double lat;
    private double lon;
    private String address;
    private boolean wifiAvailable;
    private boolean workSpace;
    private String openHours;    // "08:00"
    private String closeHours;   // "23:00"
    private String phone;
    private double minPrice;
    private String img;
    private String description;
    private long rating;
    private long totalRating;

    private double distance; // Tính khi hiển thị

    public CafeModel() {
        // Firebase cần constructor rỗng
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isWifiAvailable() { return wifiAvailable; }
    public void setWifiAvailable(boolean wifiAvailable) { this.wifiAvailable = wifiAvailable; }

    public boolean isWorkSpace() { return workSpace; }
    public void setWorkSpace(boolean workSpace) { this.workSpace = workSpace; }

    public String getOpenHours() { return openHours; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }

    public String getCloseHours() { return closeHours; }
    public void setCloseHours(String closeHours) { this.closeHours = closeHours; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getMinPrice() { return minPrice; }
    public void setMinPrice(double minPrice) { this.minPrice = minPrice; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getRating() { return rating; }
    public void setRating(long rating) { this.rating = rating; }

    public long getTotalRating() { return totalRating; }
    public void setTotalRating(long totalRating) { this.totalRating = totalRating; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    // Kiểm tra xem quán có đang mở cửa không
    public boolean isOpen() {
        try {
            String[] openSplit = openHours.split(":");
            String[] closeSplit = closeHours.split(":");

            int openMins = Integer.parseInt(openSplit[0]) * 60 + Integer.parseInt(openSplit[1]);
            int closeMins = Integer.parseInt(closeSplit[0]) * 60 + Integer.parseInt(closeSplit[1]);

            Calendar now = Calendar.getInstance();
            int nowMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

            return nowMins >= openMins && nowMins < closeMins;
        } catch (Exception e) {
            return true; // fallback nếu lỗi định dạng
        }
    }
}