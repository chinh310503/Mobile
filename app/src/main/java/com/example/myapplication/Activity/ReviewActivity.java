package com.example.myapplication.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.ImageAdapter;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private TextView txtCafeName;
    private RatingBar ratingBar;
    private EditText edtExperience;
    private RecyclerView recyclerImages;
    private MaterialButton btnSubmitReview;
    private ImageButton btnClose;

    private ImageAdapter imageAdapter;
    private List<Uri> imageList = new ArrayList<>();
    private static final int PICK_IMAGE = 1;

    private FirebaseFirestore db;
    private int cafeId = -1;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        txtCafeName = findViewById(R.id.txtCafeName);
        ratingBar = findViewById(R.id.ratingBar);
        edtExperience = findViewById(R.id.edtExperience);
        recyclerImages = findViewById(R.id.recyclerImages);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnClose = findViewById(R.id.btnClose);

        imageAdapter = new ImageAdapter(imageList, this);
        recyclerImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerImages.setAdapter(imageAdapter);

        db = FirebaseFirestore.getInstance();

        cafeId = getIntent().getIntExtra("cafe_id", -1);
        String cafeName = getIntent().getStringExtra("cafe_name");

        // ✅ Lấy userId từ SessionManager thay vì Intent
        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        Log.d("ReviewActivity", "userId = " + userId + ", cafeId = " + cafeId);

        if (cafeName != null) {
            txtCafeName.setText(cafeName);
        }

        btnClose.setOnClickListener(v -> finish());
        btnSubmitReview.setOnClickListener(v -> submitReview());

        imageAdapter.setOnAddClickListener(this::openGallery);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageList.add(data.getData());
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = edtExperience.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cafeId == -1 || userId == -1) {
            Toast.makeText(this, "Không xác định được quán cafe hoặc người dùng", Toast.LENGTH_SHORT).show();
            Log.e("ReviewActivity", "Dữ liệu không hợp lệ: cafeId=" + cafeId + ", userId=" + userId);
            return;
        }

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("id_cafe", cafeId);
        reviewData.put("star", (int) rating);
        reviewData.put("comment", comment);
        reviewData.put("id_user", userId);
        reviewData.put("created_at", Timestamp.now());

        db.collection("rate")
                .add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
