package com.example.myapplication.DAO;

import android.content.Context;
import android.net.Uri;

import com.example.myapplication.Cloudinary.CloudinaryHelper;
import com.example.myapplication.Model.ViewReviewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReviewDAO {

    private final FirebaseFirestore db;

    public ReviewDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getAllReviews(int cafeId, int userId, ReviewCallback callback) {
        db.collection("rate")
                .whereEqualTo("id_cafe", cafeId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(task -> {
                    List<ViewReviewModel> list = new ArrayList<>();
                    AtomicInteger completedCount = new AtomicInteger(0);
                    int total = task.getDocuments().size();

                    if (total == 0) {
                        callback.onSuccess(list);
                        return;
                    }

                    for (DocumentSnapshot doc : task.getDocuments()) {
                        Long uid = doc.getLong("id_user");
                        Long cid = doc.getLong("id_cafe");
                        Double star = doc.getDouble("star");
                        Long reviewNumericId = doc.getLong("id");
                        Timestamp createdAt = doc.getTimestamp("created_at");
                        String content = doc.getString("content");

                        if (uid == null || cid == null || star == null || createdAt == null || reviewNumericId == null) {
                            if (completedCount.incrementAndGet() == total) {
                                callback.onSuccess(list);
                            }
                            continue;
                        }

                        ViewReviewModel review = new ViewReviewModel();
                        review.setId(String.valueOf(reviewNumericId));
                        review.setUserId(uid.intValue());
                        review.setCafeId(cid.intValue());
                        review.setRating(star.floatValue());
                        review.setCreatedAt(createdAt);
                        review.setContent(content != null ? content : "");

                        loadUserInfo(review.getUserId(), userInfo -> {
                            review.setUserName((String) userInfo.get("name"));
                            review.setUserAvatar((String) userInfo.get("img"));

                            loadReviewImages(doc.getId(), urls -> {
                                review.setImageUrls(urls);

                                getLikeCount(review.getId(), count -> {
                                    review.setLikeCount(count);

                                    checkIfUserLiked(review.getId(), userId, liked -> {
                                        review.setLiked(liked);

                                        getCommentCount(review.getId(), commentCount -> {
                                            review.setComments(commentCount);
                                            list.add(review);

                                            if (completedCount.incrementAndGet() == total) {
                                                callback.onSuccess(list);
                                            }
                                        });
                                    });
                                });
                            });
                        });
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void loadUserInfo(int userId, OnUserInfoListener listener) {
        db.collection("users")
                .document(String.valueOf(userId))
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", doc.getString("name"));
                    map.put("img", doc.getString("img"));
                    listener.onComplete(map);
                });
    }

    public interface OnUserInfoListener {
        void onComplete(Map<String, Object> userInfo);
    }

    private void loadReviewImages(String reviewId, OnImageLoadListener listener) {
        db.collection("review_images")
                .whereEqualTo("review_id", reviewId)
                .get()
                .addOnSuccessListener(query -> {
                    List<String> urls = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        urls.add(doc.getString("img"));
                    }
                    listener.onComplete(urls);
                });
    }

    public interface OnImageLoadListener {
        void onComplete(List<String> urls);
    }

    private void getLikeCount(String reviewId, OnCountComplete listener) {
        db.collection("review_interact")
                .whereEqualTo("id_rate", Integer.parseInt(reviewId))
                .get()
                .addOnSuccessListener(task -> {
                    int count = 0;
                    for (DocumentSnapshot doc : task.getDocuments()) {
                        Boolean liked = doc.getBoolean("likes");
                        if (liked != null && liked) count++;
                    }
                    listener.onComplete(count);
                });
    }

    private void checkIfUserLiked(String reviewId, int userId, OnLikedCheckListener listener) {
        db.collection("review_interact")
                .whereEqualTo("id_rate", Integer.parseInt(reviewId))
                .whereEqualTo("id_user", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(task -> {
                    boolean liked = false;
                    if (!task.isEmpty()) {
                        Boolean likeVal = task.getDocuments().get(0).getBoolean("likes");
                        liked = likeVal != null && likeVal;
                    }
                    listener.onResult(liked);
                });
    }

    public interface OnLikedCheckListener {
        void onResult(boolean liked);
    }

    private void getCommentCount(String reviewId, OnCountComplete listener) {
        db.collection("review_comment")
                .whereEqualTo("id_rate", Integer.parseInt(reviewId))
                .get()
                .addOnSuccessListener(task -> {
                    listener.onComplete(task.size());
                });
    }

    public interface OnCountComplete {
        void onComplete(int count);
    }

    public interface ReviewCallback {
        void onSuccess(List<ViewReviewModel> list);
        void onFailure(Exception e);
    }

    public interface ToggleLikeListener {
        void onComplete(boolean isLikedNow);
    }

    public void toggleLike(String reviewId, int userId) {
        db.collection("review_interact")
                .whereEqualTo("id_rate", Integer.parseInt(reviewId))
                .whereEqualTo("id_user", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.isEmpty()) {
                        DocumentSnapshot doc = task.getDocuments().get(0);
                        boolean liked = Boolean.TRUE.equals(doc.getBoolean("likes"));
                        if (liked) {
                            db.collection("review_interact")
                                    .document(doc.getId())
                                    .delete();
                        } else {
                            db.collection("review_interact")
                                    .document(doc.getId())
                                    .update("likes", true);
                        }
                    } else {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id_rate", Integer.parseInt(reviewId));
                        map.put("id_user", userId);
                        map.put("likes", true);
                        db.collection("review_interact").add(map);
                    }
                });
    }

    public interface OnUploadReviewListener {
        void onComplete(boolean success);
    }

    public void uploadReview(Context context, float rating, String content, List<Uri> imageUris, OnUploadReviewListener listener) {
        int userId = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getInt("id_user", -1);
        int cafeId = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getInt("selected_cafe_id", -1);

        db.collection("rate")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    int newId = 1;
                    if (!query.isEmpty()) {
                        Long lastId = query.getDocuments().get(0).getLong("id");
                        if (lastId != null) newId = lastId.intValue() + 1;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", newId);
                    data.put("id_user", userId);
                    data.put("id_cafe", cafeId);
                    data.put("star", rating);
                    data.put("created_at", Timestamp.now());
                    data.put("content", content);

                    int finalNewId = newId;
                    db.collection("rate")
                            .add(data)
                            .addOnSuccessListener(documentReference -> {
                                String reviewDocId = documentReference.getId();

                                if (imageUris.isEmpty()) {
                                    listener.onComplete(true);
                                    return;
                                }

                                List<String> uploaded = new ArrayList<>();
                                for (Uri uri : imageUris) {
                                    CloudinaryHelper.uploadImage(uri, context, new CloudinaryHelper.UploadImageCallback() {
                                        @Override
                                        public void onComplete(String url) {
                                            if (url != null) {
                                                Map<String, Object> imageData = new HashMap<>();
                                                imageData.put("review_id", reviewDocId);
                                                imageData.put("img", url);
                                                db.collection("review_images").add(imageData);
                                            }
                                            uploaded.add("ok");
                                            if (uploaded.size() == imageUris.size()) {
                                                listener.onComplete(true);
                                            }
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(e -> listener.onComplete(false));
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }
}
