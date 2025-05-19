package com.example.myapplication.Model;

import com.google.firebase.Timestamp;

public class CommentReviewModel {
    private String id;
    private String reviewId; // Document ID của bài đánh giá (dạng String)
    private int userId;
    private String userName;
    private String userAvatar;
    private String content;
    private Timestamp createdAt;

    private int likeCount;       // ✅ số lượt like
    private int replyCount;      // ✅ số bình luận con (nếu có reply)
    private boolean liked;       // ✅ trạng thái người dùng hiện tại đã like hay chưa

    // Constructor rỗng cần cho Firebase
    public CommentReviewModel() {
    }

    public CommentReviewModel(String id, String reviewId, int userId, String userName, String userAvatar, String content, Timestamp createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getter & Setter cho trường cơ bản
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
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

    // Getter & Setter cho likeCount
    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    // Getter & Setter cho replyCount
    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    // Getter & Setter cho liked
    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
