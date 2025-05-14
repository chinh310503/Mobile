package com.example.myapplication.Model;

import com.google.firebase.Timestamp;
import java.util.List;

public class ViewReviewModel {
    private String id;
    private int userId;
    private String userName;
    private String userAvatar;
    private int cafeId;
    private String content;
    private float rating;
    private Timestamp createdAt;
    private int comments;
    private List<String> imageUrls;

    // Bổ sung các trường mới
    private boolean liked;
    private int likeCount;

    public ViewReviewModel() {
        // Constructor rỗng cho Firestore
    }

    public ViewReviewModel(String id, int userId, String userName, String userAvatar,
                           int cafeId, String content, float rating,
                           Timestamp createdAt, int comments, List<String> imageUrls,
                           boolean liked, int likeCount) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.cafeId = cafeId;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
        this.comments = comments;
        this.imageUrls = imageUrls;
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public int getCafeId() {
        return cafeId;
    }

    public void setCafeId(int cafeId) {
        this.cafeId = cafeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // ✅ Getter / Setter cho like
    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
