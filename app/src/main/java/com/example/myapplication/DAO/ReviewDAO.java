package com.example.myapplication.DAO;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.myapplication.Model.ViewReviewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ReviewDAO {
    private final FirebaseFirestore db;
    private final Context context;

    public ReviewDAO(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public interface ReviewCallback {
        void onSuccess(List<ViewReviewModel> list);
        void onFailure(Exception e);
    }

    public void getAllReviews(int cafeId, int userId, ReviewCallback callback) {
        db.collection("reviews")
                .whereEqualTo("cafe_id", cafeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ViewReviewModel> list = new ArrayList<>();
                    List<Task<Void>> tasks = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ViewReviewModel model = new ViewReviewModel();
                        model.setId(doc.getLong("id").intValue());
                        model.setCafeId(cafeId);
                        model.setUserId(doc.getLong("user_id").intValue());
                        model.setRating(doc.getDouble("rating").floatValue());
                        model.setContent(doc.getString("content"));
                        model.setCreatedAt(doc.getTimestamp("created_at"));
                        model.setImageUrls(new ArrayList<>());

                        Task<Void> getUserTask = getUserInfo(model.getUserId(), model);
                        tasks.add(getUserTask);

                        getImagesForReview(model.getId(), model);
                        getLikeCount(model.getId(), count -> model.setLikeCount(count));
                        getCommentCount(model.getId(), count -> model.setCommentCount(count));

                        TaskCompletionSource<Void> likedSource = new TaskCompletionSource<>();
                        checkIfLiked(model.getId(), userId, liked -> {
                            model.setLiked(liked);
                            likedSource.setResult(null);
                        });
                        tasks.add(likedSource.getTask());

                        list.add(model);
                    }

                    Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> callback.onSuccess(list));
                })
                .addOnFailureListener(callback::onFailure);
    }

    private Task<Void> getUserInfo(int userId, ViewReviewModel model) {
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        db.collection("users").whereEqualTo("id", userId).limit(1).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        model.setUserName(doc.getString("name"));
                        model.setUserAvatar(doc.getString("img"));
                    }
                    source.setResult(null);
                })
                .addOnFailureListener(source::setException);
        return source.getTask();
    }

    private void getImagesForReview(int reviewId, ViewReviewModel model) {
        db.collection("review_images")
                .whereEqualTo("review_id", reviewId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> urls = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        urls.add(doc.getString("img"));
                    }
                    model.setImageUrls(urls);
                });
    }

    private void getLikeCount(int reviewId, OnSuccessListener<Integer> listener) {
        db.collection("interact_review")
                .whereEqualTo("review_id", reviewId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(snapshot -> listener.onSuccess(snapshot.size()));
    }

    private void getCommentCount(int reviewId, OnSuccessListener<Integer> listener) {
        db.collection("interact_review")
                .whereEqualTo("review_id", reviewId)
                .whereNotEqualTo("comment", null)
                .get()
                .addOnSuccessListener(snapshot -> listener.onSuccess(snapshot.size()));
    }

    private void checkIfLiked(int reviewId, int userId, OnSuccessListener<Boolean> listener) {
        db.collection("interact_review")
                .whereEqualTo("review_id", reviewId)
                .whereEqualTo("user_id", userId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(snapshot -> listener.onSuccess(!snapshot.isEmpty()));
    }

    public void toggleLike(int reviewId, int userId) {
        db.collection("interact_review")
                .whereEqualTo("review_id", reviewId)
                .whereEqualTo("user_id", userId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        for (DocumentSnapshot doc : snapshot) {
                            db.collection("interact_review").document(doc.getId()).delete();
                        }
                    } else {
                        Map<String, Object> likeData = new HashMap<>();
                        likeData.put("review_id", reviewId);
                        likeData.put("user_id", userId);
                        likeData.put("likes", true);
                        db.collection("interact_review").add(likeData);
                    }
                });
    }

    public void addReview(int reviewId, int cafeId, int userId, float rating, String content, List<Uri> imageUris,
                          OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {

        Map<String, Object> data = new HashMap<>();
        data.put("id", reviewId);
        data.put("cafe_id", cafeId);
        data.put("user_id", userId);
        data.put("rating", rating);
        data.put("content", content);
        data.put("created_at", com.google.firebase.Timestamp.now());

        db.collection("reviews").add(data)
                .addOnSuccessListener(docRef -> {
                    if (imageUris != null && !imageUris.isEmpty()) {
                        uploadImages(reviewId, imageUris, onSuccess, onFailure);
                    } else {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void uploadImages(int reviewId, List<Uri> imageUris,
                             OnSuccessListener<Void> onSuccess,
                             OnFailureListener onFailure) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicBoolean hasError = new AtomicBoolean(false);

        for (Uri uri : imageUris) {
            MediaManager.get().upload(uri)
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            if (hasError.get()) return;

                            String url = resultData.get("secure_url").toString();
                            Map<String, Object> image = new HashMap<>();
                            image.put("review_id", reviewId);
                            image.put("img", url);

                            db.collection("review_images").add(image)
                                    .addOnSuccessListener(docRef -> {
                                        if (successCount.incrementAndGet() == imageUris.size()) {
                                            onSuccess.onSuccess(null);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (hasError.compareAndSet(false, true)) {
                                            onFailure.onFailure(e);
                                        }
                                    });
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            if (hasError.compareAndSet(false, true)) {
                                onFailure.onFailure(new Exception(error.getDescription()));
                            }
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    })
                    .dispatch();
        }
    }
}
