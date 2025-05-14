package com.example.myapplication.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.Adapter.CommentAdapter;
import com.example.myapplication.Adapter.ImagePagerAdapter;
import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.Model.PostModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private TextView tvUserName, tvTime, tvContent, tvLikeCount, tvCommentCount;
    private ImageView ivAvatar, ivLike, ivComment, ivMenu;
    private ViewPager2 viewPagerImages;
    private RecyclerView rvComments;
    private EditText edtComment;
    private ImageButton btnSend;
    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList = new ArrayList<>();
    private FirebaseFirestore db;
    private int postId;
    private SessionManager sessionManager;
    private DotsIndicator dotsIndicator;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ivAvatar = findViewById(R.id.ivAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvTime = findViewById(R.id.tvTime);
        tvContent = findViewById(R.id.tvContent);
        tvLikeCount = findViewById(R.id.tvLikeCount);
        tvCommentCount = findViewById(R.id.tvCommentCount);
        ivLike = findViewById(R.id.ivLike);
        ivComment = findViewById(R.id.ivComment);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        ivMenu = findViewById(R.id.ivMenu);
        rvComments = findViewById(R.id.rvComments);
        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        postId = getIntent().getIntExtra("postId", -1);

        commentAdapter = new CommentAdapter(this, commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        loadPost();
        loadComments();

        btnSend.setOnClickListener(v -> sendComment());

        ivLike.setOnClickListener(v -> toggleLike());
    }

    private void loadPost() {
        db.collection("post")
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        var doc = snapshot.getDocuments().get(0);
                        PostModel post = new PostModel();
                        post.setId(doc.getLong("id").intValue());
                        post.setUserId(doc.getLong("id_user").intValue());
                        post.setContent(doc.getString("content"));
                        post.setTimestamp(doc.getLong("timestamp"));
                        int userId = doc.getLong("id_user").intValue();
                        SessionManager sessionManager = new SessionManager(this);
                        if (post.getUserId() == sessionManager.getUserId()) {
                            ivMenu.setVisibility(View.VISIBLE);
                            ivMenu.setOnClickListener(v -> {
                                PopupMenu popup = new PopupMenu(this, ivMenu);
                                popup.inflate(R.menu.menu_post_options);
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.action_delete) {
                                        showDeleteConfirmation(post.getId());
                                        return true;
                                    } else if (item.getItemId() == R.id.action_edit) {
                                        Intent intent = new Intent(CommentActivity.this, EditPostActivity.class);
                                        intent.putExtra("postId", post.getId());
                                        startActivity(intent);
                                        return true;
                                    }
                                    return false;
                                });
                                popup.show();
                            });
                        } else {
                            ivMenu.setVisibility(View.GONE);
                        }
                        db.collection("users")
                                .whereEqualTo("id", userId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(userSnap -> {
                                    if (!userSnap.isEmpty()) {
                                        String name = userSnap.getDocuments().get(0).getString("name");
                                        String avatar = userSnap.getDocuments().get(0).getString("img");
                                        post.setUserName(name);
                                        post.setUserAvatar(avatar);

                                        tvUserName.setText(name);
                                        Glide.with(this)
                                                .load(avatar)
                                                .placeholder(R.drawable.ic_user)
                                                .circleCrop()
                                                .into(ivAvatar);
                                    }
                                });

                        tvContent.setText(post.getContent());
                        if (post.getTimestamp() > 0) {
                            Date date = new Date(post.getTimestamp());
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                            tvTime.setText(sdf.format(date));
                        }

                        db.collection("post_image")
                                .whereEqualTo("id_post", postId)
                                .get()
                                .addOnSuccessListener(imageSnap -> {
                                    List<String> imageList = new ArrayList<>();
                                    for (var imageDoc : imageSnap) {
                                        imageList.add(imageDoc.getString("img"));
                                    }
                                    if (!imageList.isEmpty()) {
                                        viewPagerImages.setVisibility(View.VISIBLE);
                                        //viewPagerImages.setAdapter(new ImagePagerAdapter(this, imageList));
                                        ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageList);
                                        viewPagerImages.setAdapter(adapter);

                                        if (imageList.size() > 1) {
                                            dotsIndicator.setVisibility(View.VISIBLE);
                                            dotsIndicator.setViewPager2(viewPagerImages);
                                        } else {
                                            dotsIndicator.setVisibility(View.GONE);
                                        }
                                    }
                                });

                        db.collection("interact")
                                .whereEqualTo("id_post", postId)
                                .whereEqualTo("likes", true)
                                .get()
                                .addOnSuccessListener(likeSnap -> {
                                    tvLikeCount.setText(String.valueOf(likeSnap.size()));

                                    db.collection("interact")
                                            .whereEqualTo("id_post", postId)
                                            .whereEqualTo("id_user", sessionManager.getUserId())
                                            .whereEqualTo("likes", true)
                                            .get()
                                            .addOnSuccessListener(userLikeSnap -> {
                                                boolean liked = !userLikeSnap.isEmpty();
                                                ivLike.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                                                ivLike.setTag(liked ? "liked" : "unliked");
                                            });
                                });

                        db.collection("comment")
                                .whereEqualTo("postId", postId)
                                .get()
                                .addOnSuccessListener(commentSnap -> {
                                    tvCommentCount.setText(String.valueOf(commentSnap.size()));
                                });
                    }
                });

    }

    private void toggleLike() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean wasLiked = ivLike.getTag() != null && ivLike.getTag().equals("liked");
        ivLike.setImageResource(wasLiked ? R.drawable.ic_heart_outline : R.drawable.ic_heart_filled);
        ivLike.setTag(wasLiked ? "unliked" : "liked");

        int currentLikes = Integer.parseInt(tvLikeCount.getText().toString());
        tvLikeCount.setText(String.valueOf(wasLiked ? currentLikes - 1 : currentLikes + 1));

        db.collection("interact")
                .whereEqualTo("id_post", postId)
                .whereEqualTo("id_user", userId)
                .whereEqualTo("likes", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        snapshot.getDocuments().forEach(doc ->
                                db.collection("interact").document(doc.getId()).delete());
                    } else {
                        Map<String, Object> likeData = new HashMap<>();
                        likeData.put("id_post", postId);
                        likeData.put("id_user", userId);
                        likeData.put("likes", true);
                        db.collection("interact").add(likeData);
                    }
                });
    }

    private void loadComments() {
        db.collection("comment")
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null) {
                        commentList.clear();
                        for (var doc : snapshot) {
                            CommentModel c = new CommentModel();
                            c.id = doc.getId();
                            c.postId = postId;
                            c.userId = doc.getLong("userId").intValue();
                            c.content = doc.getString("content");
                            c.timestamp = doc.getLong("timestamp");

                            db.collection("users")
                                    .whereEqualTo("id", c.userId)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(userSnap -> {
                                        if (!userSnap.isEmpty()) {
                                            c.userName = userSnap.getDocuments().get(0).getString("name");
                                            c.userAvatar = userSnap.getDocuments().get(0).getString("img");
                                            commentList.add(c);
                                            commentAdapter.notifyItemInserted(commentList.size() - 1);
                                        }
                                    });
                        }
                    }
                });
    }
    private void showDeleteConfirmation(int postId) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    PostDAO dao = new PostDAO();
                    dao.deletePostById(postId,
                            () -> {
                                Toast.makeText(this, "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                                finish(); // Đóng CommentActivity sau khi xóa
                            },
                            e -> Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void sendComment() {
        String commentText = edtComment.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Nội dung bình luận trống", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin người dùng hiện tại để hiển thị ngay
        String userName = sessionManager.getUserName();
        String userAvatar = sessionManager.getUserImg();

        CommentModel newComment = new CommentModel();
        newComment.id = "temp_" + System.currentTimeMillis();
        newComment.postId = postId;
        newComment.userId = userId;
        newComment.userName = userName;
        newComment.userAvatar = userAvatar;
        newComment.content = commentText;
        newComment.timestamp = System.currentTimeMillis();

        commentList.add(newComment);
        commentAdapter.notifyItemInserted(commentList.size() - 1);
        rvComments.scrollToPosition(commentList.size() - 1);

        edtComment.setText("");

        // Ẩn bàn phím sau khi gửi bình luận
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(edtComment.getWindowToken(), 0);
        }

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("postId", postId);
        commentData.put("userId", userId);
        commentData.put("content", commentText);
        commentData.put("timestamp", newComment.timestamp);

        db.collection("comment")
                .add(commentData)
                .addOnSuccessListener(docRef -> {
                    // Không cần làm gì thêm vì đã hiển thị bình luận trước đó
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi bình luận", Toast.LENGTH_SHORT).show());
    }
}