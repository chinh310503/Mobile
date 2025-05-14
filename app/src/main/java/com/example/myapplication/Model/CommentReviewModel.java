package com.example.myapplication.Model;

import com.google.firebase.Timestamp;

public class CommentReviewModel {
    public String id;
    public int reviewId;
    public int userId;
    public String userName;
    public String userAvatar;
    public String content;
    public Timestamp createdAt;

    public CommentReviewModel() {
    }

    public CommentReviewModel(String id, int reviewId, int userId, String userName, String userAvatar, String content, Timestamp createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
