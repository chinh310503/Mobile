package com.example.myapplication.DAO;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.Model.PostModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDAO {
    private final FirebaseFirestore db;

    public interface PostListCallback {
        void onSuccess(List<PostModel> posts);
        void onFailure(Exception e);
    }

    public PostDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllPosts(int userId, PostListCallback callback) {
        db.collection("post").get()
                .addOnSuccessListener(postsSnapshot -> {
                    List<PostModel> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : postsSnapshot) {
                        PostModel post = new PostModel();
                        post.setId(doc.getLong("id").intValue());
                        post.setContent(doc.getString("content"));
                        post.setUserName("User #" + doc.getLong("id_user")); // nên map từ bảng user nếu có
                        post.setUserAvatar("avatar_placeholder_url");
                        post.setImageList(new ArrayList<>());

                        int postId = post.getId();
                        getImagesForPost(postId, post);
                        getLikeCount(postId, count -> post.setLikeCount(count));
                        getCommentCount(postId, count -> post.setCommentCount(count));
                        checkIfLiked(postId, userId, liked -> post.setLiked(liked));

                        posts.add(post);
                    }
                    callback.onSuccess(posts);
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void getImagesForPost(int postId, PostModel post) {
        db.collection("post_image")
                .whereEqualTo("id_post", postId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> images = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        images.add(doc.getString("img"));
                    }
                    post.setImageList(images);
                });
    }

    private void getLikeCount(int postId, final OnSuccessListener<Integer> listener) {
        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> listener.onSuccess(queryDocumentSnapshots.size()));
    }

    private void getCommentCount(int postId, final OnSuccessListener<Integer> listener) {
        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereNotEqualTo("comment", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> listener.onSuccess(queryDocumentSnapshots.size()));
    }

    private void checkIfLiked(int postId, int userId, final OnSuccessListener<Boolean> listener) {
        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereEqualTo("id_user", userId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(snapshot -> listener.onSuccess(!snapshot.isEmpty()));
    }

    public void insertPost(int userId, String content, List<Uri> imageUris, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        int newPostId = (int) (System.currentTimeMillis() / 1000); // dùng timestamp đơn giản
        Map<String, Object> post = new HashMap<>();
        post.put("id", newPostId);
        post.put("id_user", userId);
        post.put("content", content);

        db.collection("post").add(post)
                .addOnSuccessListener(documentReference -> {
                    if (imageUris != null) {
                        for (Uri uri : imageUris) {
                            Map<String, Object> imgData = new HashMap<>();
                            imgData.put("id_post", newPostId);
                            imgData.put("img", uri.toString());
                            db.collection("post_image").add(imgData);
                        }
                    }
                    onSuccess.onSuccess(null);
                })
                .addOnFailureListener(onFailure);
    }

    public void toggleLike(int postId, int userId) {
        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereEqualTo("id_user", userId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        for (DocumentSnapshot doc : snapshot) {
                            db.collection("interact").document(doc.getId()).delete();
                        }
                    } else {
                        Map<String, Object> likeData = new HashMap<>();
                        likeData.put("id_post", postId);
                        likeData.put("id_user", userId);
                        likeData.put("likes", true);
                        db.collection("interact").add(likeData);
                    }
                });
    }
}
