package com.example.myapplication.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.CommentReviewAdapter;
import com.example.myapplication.Model.CommentReviewModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_review_activity);

        recyclerView = findViewById(R.id.rvComments);
        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);

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
            public void onLikeComment(CommentReviewModel comment) {
                // TODO: xử lý thích bình luận nếu muốn
            }

            @Override
            public void onReplyComment(CommentReviewModel comment) {
                // TODO: xử lý phản hồi bình luận nếu muốn
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadComments();
        btnSend.setOnClickListener(v -> sendComment());
    }

    private void loadComments() {
        db.collection("review_comment")
                .whereEqualTo("review_id", reviewId)
                .orderBy("created_at")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải bình luận: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null) {
                        commentList.clear();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            CommentReviewModel comment = new CommentReviewModel();
                            comment.setId(doc.getId());
                            comment.setReviewId(reviewId);

                            Long userId = doc.getLong("user_id");
                            comment.setUserId(userId != null ? userId.intValue() : -1);

                            comment.setUserName(doc.getString("user_name"));
                            comment.setUserAvatar(doc.getString("user_avatar"));
                            comment.setContent(doc.getString("content"));
                            comment.setCreatedAt(doc.getTimestamp("created_at"));

                            // Optional: future support
                            Long likeCount = doc.getLong("like_count");
                            comment.setLikeCount(likeCount != null ? likeCount.intValue() : 0);

                            Long replyCount = doc.getLong("reply_count");
                            comment.setReplyCount(replyCount != null ? replyCount.intValue() : 0);

                            Boolean liked = doc.getBoolean("liked");
                            comment.setLiked(liked != null ? liked : false);

                            commentList.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                        if (!commentList.isEmpty()) {
                            recyclerView.scrollToPosition(commentList.size() - 1);
                        }
                    }
                });
    }

    private void sendComment() {
        String content = edtComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Nội dung trống", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        String userName = sessionManager.getUserName();
        String userAvatar = sessionManager.getUserImg();

        Map<String, Object> data = new HashMap<>();
        data.put("review_id", reviewId); // ✅ THỐNG NHẤT field
        data.put("user_id", userId);
        data.put("user_name", userName);
        data.put("user_avatar", userAvatar);
        data.put("content", content);
        data.put("created_at", Timestamp.now());

        db.collection("review_comment")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    edtComment.setText("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(edtComment.getWindowToken(), 0);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi bình luận", Toast.LENGTH_SHORT).show());
    }
}
