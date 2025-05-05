package com.example.myapplication.DAO;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.UploadResult;
import android.content.ContentResolver;
import com.example.myapplication.Cloudinary.CloudinaryHelper;
import com.example.myapplication.Model.PostModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.database.Cursor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PostDAO {
    private FirebaseFirestore db;
    private Context context;
    public interface PostListCallback {
        void onSuccess(List<PostModel> posts);
        void onFailure(Exception e);
    }

    public PostDAO() {
        db = FirebaseFirestore.getInstance();
    }
    public PostDAO(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    // Lấy tất cả bài viết từ Firestore
    public void getAllPosts(int userId, PostListCallback callback) {
        Log.d("PostDAO", "Current userId: " + userId);
        db.collection("post").get()
                .addOnSuccessListener(postsSnapshot -> {
                    List<PostModel> posts = new ArrayList<>();
                    List<Task<Void>> tasks = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : postsSnapshot) {
                        PostModel post = new PostModel();
                        post.setId(doc.getLong("id").intValue());
                        post.setContent(doc.getString("content"));
                        post.setImageList(new ArrayList<>());

                        int postId = post.getId();
                        int userIdFromPost = doc.getLong("id_user").intValue();

                        // Lấy tên người đăng bài
                        Task<Void> getUserTask = getUserNameForPost(userIdFromPost, post);
                        tasks.add(getUserTask);

                        // Lấy ảnh bài viết
                        getImagesForPost(postId, post);

                        // Lấy số lượt like
                        getLikeCount(postId, count -> post.setLikeCount(count));

                        // Lấy số lượt comment
                        getCommentCount(postId, count -> post.setCommentCount(count));

                        // Kiểm tra bài viết đã like chưa
                        TaskCompletionSource<Void> likeTaskSource = new TaskCompletionSource<>();
                        checkIfLiked(postId, userId, liked -> {
                            post.setLiked(liked);
                            likeTaskSource.setResult(null);
                        });
                        tasks.add(likeTaskSource.getTask());

                        posts.add(post);
                    }

                    // Đợi tất cả các task hoàn thành
                    Tasks.whenAllSuccess(tasks).addOnCompleteListener(task -> {
                        callback.onSuccess(posts);
                    });
                })
                .addOnFailureListener(callback::onFailure);
    }


    private Task<Void> getUserNameForPost(int userId, PostModel post) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        db.collection("users")
                .whereEqualTo("id", userId)  // Tìm theo trường "id" chứ không tìm document ID
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String name = snapshot.getDocuments().get(0).getString("name");
                        String avatar = snapshot.getDocuments().get(0).getString("img");
                        post.setUserName(name != null ? name : "No name");
                        post.setUserAvatar(avatar != null ? avatar : "");
                    } else {
                        post.setUserName("User not found");
                    }
                    taskCompletionSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    post.setUserName("Error loading user name");
                    taskCompletionSource.setException(e);
                });

        return taskCompletionSource.getTask();
    }

    // Lấy các ảnh cho bài viết từ Firestore
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
    // Lấy số lượt like của bài viết
    private void getLikeCount(int postId, final OnSuccessListener<Integer> listener) {
        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> listener.onSuccess(queryDocumentSnapshots.size()));
    }
    // Lấy số bình luận của bài viết
    private void getCommentCount(int postId, final OnSuccessListener<Integer> listener) {
        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereNotEqualTo("comment", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> listener.onSuccess(queryDocumentSnapshots.size()));
    }
    // Kiểm tra xem người dùng đã like bài viết hay chưa
    private void checkIfLiked(int postId, int userId, final OnSuccessListener<Boolean> listener) {
        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereEqualTo("id_user", userId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(snapshot -> listener.onSuccess(!snapshot.isEmpty()));
    }
    // Thêm bài viết mới vào Firebase
    public void insertPost(Context context, int userId, String content, List<Uri> imageUris,
                           OnSuccessListener<Void> onSuccess,
                           OnFailureListener onFailure) {

        int newPostId = (int) (System.currentTimeMillis() / 1000);
        Map<String, Object> post = new HashMap<>();
        post.put("id", newPostId);
        post.put("id_user", userId);
        post.put("content", content);
        post.put("timestamp", System.currentTimeMillis());

        db.collection("post").add(post)
                .addOnSuccessListener(documentReference -> {
                    if (imageUris != null && !imageUris.isEmpty()) {
                        uploadImages(context, newPostId, imageUris, onSuccess, onFailure);
                    } else {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(onFailure);
    }



    // Hàm upload ảnh lên Cloudinary
    public void uploadImages(Context context, int postId, List<Uri> imageUris,
                             OnSuccessListener<Void> onSuccess,
                             OnFailureListener onFailure) {


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicBoolean hasErrorOccurred = new AtomicBoolean(false);

        for (Uri uri : imageUris) {
            MediaManager.get().upload(uri)
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            // Bắt đầu upload
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Có thể hiện tiến trình nếu cần
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            if (hasErrorOccurred.get()) return;

                            String downloadUrl = resultData.get("secure_url").toString();
                            Map<String, Object> imgData = new HashMap<>();
                            imgData.put("id_post", postId);
                            imgData.put("img", downloadUrl);

                            db.collection("post_image").add(imgData)
                                    .addOnSuccessListener(documentReference -> {
                                        if (successCount.incrementAndGet() == imageUris.size()) {
                                            onSuccess.onSuccess(null); // tất cả ảnh đã xong
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (hasErrorOccurred.compareAndSet(false, true)) {
                                            onFailure.onFailure(e);
                                        }
                                    });
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            if (hasErrorOccurred.compareAndSet(false, true)) {
                                onFailure.onFailure(new Exception(error.getDescription()));
                            }
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            // Xử lý nếu muốn retry
                        }
                    })
                    .dispatch(); // kích hoạt upload
        }
    }





    private String getRealPathFromURI(Context context, Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }





    // Toggle like/unlike cho bài viết
    public void toggleLike(int postId, int userId) {
        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereEqualTo("id_user", userId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        // Nếu đã like, xóa like
                        for (DocumentSnapshot doc : snapshot) {
                            db.collection("interact").document(doc.getId()).delete();
                        }
                    } else {
                        // Nếu chưa like, thêm like
                        Map<String, Object> likeData = new HashMap<>();
                        likeData.put("id_post", postId);
                        likeData.put("id_user", userId);
                        likeData.put("likes", true);
                        db.collection("interact").add(likeData);
                    }
                });
    }

}

