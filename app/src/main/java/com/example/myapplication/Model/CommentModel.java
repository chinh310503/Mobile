package com.example.myapplication.Model;
import com.google.firebase.Timestamp;

public class CommentModel {
    public String id;
    public int postId;
    public int userId;
    public String userName;
    public String userAvatar;
    public String content;
    public long timestamp;

    public CommentModel() {} // Firebase yêu cầu constructor rỗng

    public CommentModel(String id, int postId, int userId, String userName, String userAvatar, String content, long timestamp) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.timestamp = timestamp;
    }
}
