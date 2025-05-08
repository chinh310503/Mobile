package com.example.myapplication.Model;

import com.google.firebase.Timestamp;
import java.util.List;

public class ViewReviewModel {
    private int id;
    private int id_user;
    private int id_cafe;
    private String userName;
    private String userAvatar;
    private String content;
    private int star;
    private int likeCount;
    private int commentCount;
    private Timestamp createdAt;
    private List<String> imageUrls;

    private boolean liked;  // thêm trường liked để kiểm tra trạng thái like của user

    public ViewReviewModel() {
        // Constructor rỗng cho Firestore
    }

    // Getter & Setter cơ bản
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }

    public int getId_cafe() { return id_cafe; }
    public void setId_cafe(int id_cafe) { this.id_cafe = id_cafe; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getStar() { return star; }
    public void setStar(int star) { this.star = star; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }

    // Alias cho ReviewDAO compatibility (dễ đọc hơn và chuẩn hóa theo Android convention)

    public void setCafeId(int cafeId) {
        this.id_cafe = cafeId;
    }

    public int getCafeId() {
        return this.id_cafe;
    }

    public void setUserId(int userId) {
        this.id_user = userId;
    }

    public int getUserId() {
        return this.id_user;
    }

    public void setRating(float rating) {
        this.star = (int) rating;
    }

    public float getRating() {
        return (float) this.star;
    }
}
