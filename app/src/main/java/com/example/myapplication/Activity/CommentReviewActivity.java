package com.example.myapplication.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Adapter.CommentReviewAdapter;
import com.example.myapplication.Model.CommentReviewModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentReviewAdapter adapter;
    private List<CommentReviewModel> commentList;
    private EditText edtComment;
    private ImageButton btnSend;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String reviewId;

    private TextView tvReviewUserName, tvReviewContent, tvReviewTime, tvReviewLikeCount, tvReviewCommentCount;
    private RatingBar reviewRatingBar;
    private ImageView imgReviewImage, imgReviewUserAvatar, imgCurrentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_review_activity);

        recyclerView = findViewById(R.id.rvComments);
        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);
        tvReviewUserName = findViewById(R.id.tvReviewUserName);
        tvReviewContent = findViewById(R.id.tvReviewContent);
        tvReviewTime = findViewById(R.id.tvReviewTime);
        tvReviewLikeCount = findViewById(R.id.tvReviewLikeCount);
        tvReviewCommentCount = findViewById(R.id.tvReviewCommentCount);
        reviewRatingBar = findViewById(R.id.reviewRatingBar);
        imgReviewImage = findViewById(R.id.imgReviewImage);
        imgReviewUserAvatar = findViewById(R.id.imgReviewUserAvatar);
        imgCurrentUser = findViewById(R.id.imgCurrentUser);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        reviewId = getIntent().getStringExtra("review_id");
        if (TextUtils.isEmpty(reviewId)) {
            Toast.makeText(this, "Thiếu mã đánh giá", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        commentList = new ArrayList<>();
        adapter = new CommentReviewAdapter(this, commentList, new CommentReviewAdapter.OnCommentInteractionListener() {
            @Override
            public void onEditComment(CommentReviewModel comment, int position) {
                showEditDialog(comment, position);
            }

            @Override
            public void onDeleteComment(CommentReviewModel comment, int position) {
                deleteComment(comment.getId(), position);
            }

            @Override
            public void onReportComment(CommentReviewModel comment, int position) {
                Toast.makeText(CommentReviewActivity.this, "Đã báo cáo bình luận", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadReviewInfo();
        loadComments();
        setCurrentUserAvatar();

        btnSend.setOnClickListener(v -> sendComment());
    }

    private void loadReviewInfo() {
        db.collection("rate").document(reviewId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Bài đánh giá không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    tvReviewContent.setText(doc.getString("content") != null ? doc.getString("content") : "");
                    Timestamp createdAt = doc.getTimestamp("created_at");
                    tvReviewTime.setText(createdAt != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(createdAt.toDate()) : "");

                    Double rating = doc.getDouble("star");
                    reviewRatingBar.setRating(rating != null ? rating.floatValue() : 0);

                    Long idUser = doc.getLong("id_user");
                    if (idUser != null) {
                        db.collection("users").whereEqualTo("id", idUser.intValue()).limit(1).get()
                                .addOnSuccessListener(snapshot -> {
                                    if (!snapshot.isEmpty()) {
                                        DocumentSnapshot userDoc = snapshot.getDocuments().get(0);
                                        tvReviewUserName.setText(userDoc.getString("name"));
                                        String avatarUrl = userDoc.getString("img");
                                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                            Glide.with(this).load(avatarUrl).circleCrop().into(imgReviewUserAvatar);
                                        } else {
                                            imgReviewUserAvatar.setImageResource(R.drawable.default_avatar);
                                        }
                                    }
                                });
                    }

                    db.collection("review_images").whereEqualTo("review_id", reviewId).limit(1).get()
                            .addOnSuccessListener(snapshot -> {
                                if (!snapshot.isEmpty()) {
                                    String imgUrl = snapshot.getDocuments().get(0).getString("img");
                                    if (imgUrl != null && !imgUrl.isEmpty()) {
                                        imgReviewImage.setVisibility(ImageView.VISIBLE);
                                        Glide.with(this).load(imgUrl).into(imgReviewImage);
                                    } else {
                                        imgReviewImage.setVisibility(ImageView.GONE);
                                    }
                                } else {
                                    imgReviewImage.setVisibility(ImageView.GONE);
                                }
                            });

                    try {
                        int intReviewId = Integer.parseInt(reviewId);
                        db.collection("review_interact")
                                .whereEqualTo("id_rate", intReviewId)
                                .whereEqualTo("likes", true).get()
                                .addOnSuccessListener(snapshot -> tvReviewLikeCount.setText(String.valueOf(snapshot.size())));
                    } catch (NumberFormatException e) {
                        tvReviewLikeCount.setText("0");
                    }

                    db.collection("review_comment").whereEqualTo("review_id", reviewId).get()
                            .addOnSuccessListener(snapshot -> tvReviewCommentCount.setText(snapshot.size() + " bình luận"));
                });
    }

    private void loadComments() {
        db.collection("review_comment")
                .whereEqualTo("review_id", reviewId).orderBy("created_at").addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    commentList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        CommentReviewModel comment = new CommentReviewModel();
                        comment.setId(doc.getId());
                        comment.setReviewId(reviewId);
                        comment.setUserId(doc.getLong("user_id") != null ? doc.getLong("user_id").intValue() : -1);
                        comment.setUserName(doc.getString("user_name"));
                        comment.setUserAvatar(doc.getString("user_avatar"));
                        comment.setContent(doc.getString("content"));
                        comment.setCreatedAt(doc.getTimestamp("created_at"));
                        commentList.add(comment);
                    }
                    adapter.notifyDataSetChanged();
                    tvReviewCommentCount.setText(commentList.size() + " bình luận");
                });
    }

    private void setCurrentUserAvatar() {
        String avatarUrl = sessionManager.getUserImg();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into(imgCurrentUser);
        } else {
            imgCurrentUser.setImageResource(R.drawable.default_avatar);
        }
    }

    private void sendComment() {
        String content = edtComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Nội dung trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("review_id", reviewId);
        data.put("user_id", sessionManager.getUserId());
        data.put("user_name", sessionManager.getUserName());
        data.put("user_avatar", sessionManager.getUserImg());
        data.put("content", content);
        data.put("created_at", Timestamp.now());

        db.collection("review_comment").add(data).addOnSuccessListener(docRef -> {
            edtComment.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(edtComment.getWindowToken(), 0);
        });
    }

    private void showEditDialog(CommentReviewModel comment, int position) {
        EditText input = new EditText(this);
        input.setText(comment.getContent());
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa bình luận")
                .setView(input)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String newContent = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newContent)) {
                        db.collection("review_comment").document(comment.getId()).update("content", newContent).addOnSuccessListener(aVoid -> {
                            comment.setContent(newContent);
                            adapter.notifyItemChanged(position);
                        });
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteComment(String commentId, int position) {
        db.collection("review_comment").document(commentId).delete().addOnSuccessListener(aVoid -> {
            commentList.remove(position);
            adapter.notifyItemRemoved(position);
        });
    }
}
