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

        // ✅ Lấy reviewId kiểu String (document ID của bảng rate)
        reviewId = getIntent().getStringExtra("review_id");

        commentList = new ArrayList<>();
        adapter = new CommentReviewAdapter(this, commentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadComments();

        btnSend.setOnClickListener(v -> sendComment());
    }

    private void loadComments() {
        db.collection("review_comment")
                .whereEqualTo("id_rate", reviewId)
                .orderBy("created_at")
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null) {
                        commentList.clear();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            CommentReviewModel comment = new CommentReviewModel();
                            comment.id = doc.getId();
                            comment.reviewId = reviewId;
                            comment.userId = doc.getLong("id_user").intValue();
                            comment.userName = doc.getString("user_name");
                            comment.userAvatar = doc.getString("user_avatar");
                            comment.content = doc.getString("content");
                            comment.createdAt = doc.getTimestamp("created_at");
                            commentList.add(comment);
                        }
                        adapter.notifyDataSetChanged();
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
        data.put("id_rate", reviewId); // ✅ giữ nguyên dạng String document ID
        data.put("id_user", userId);
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
