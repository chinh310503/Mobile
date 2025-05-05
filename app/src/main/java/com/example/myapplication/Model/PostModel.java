package com.example.myapplication.Model;

import java.util.ArrayList;
import java.util.List;

public class PostModel {
    public int id;
    public String content;
    public String userName;
    public String userAvatar;
    public String image;
    public int likeCount;
    public List<String> comments;
    public String timePosted;
    public String avatarUri;
    public List<String> imageList;
    public int commentCount;
    public boolean isLiked;
    public long timestamp;

    public PostModel() {
        this.comments = new ArrayList<>();
        this.imageList = new ArrayList<>();
        this.isLiked = false;
    }

    public PostModel(int id, String content, String userName, String userAvatar, String image, int likeCount, boolean isLiked, long timestamp) {
        this.id = id;
        this.content = content;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.image = image;
        this.likeCount = likeCount;
        this.comments = new ArrayList<>();
        this.imageList = new ArrayList<>();
        this.isLiked = isLiked;
        this.timestamp = timestamp;  // Lưu thời gian tạo bài viết
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public String getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(String timePosted) {
        this.timePosted = timePosted;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
    // Getter và Setter cho timestamp
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
