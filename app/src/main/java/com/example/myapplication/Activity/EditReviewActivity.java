package com.example.myapplication.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.ImageReviewAdapter;
import com.example.myapplication.Cloudinary.CloudinaryHelper;
import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText edtContent;
    private ImageButton btnSave;
    private RecyclerView recyclerImages;
    private ImageReviewAdapter imageAdapter;
    private List<String> oldImageUrls = new ArrayList<>();
    private List<Uri> newImageUris = new ArrayList<>();

    private String reviewDocId;
    private float currentRating = 0f;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_review);

        ratingBar = findViewById(R.id.ratingBar);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);
        recyclerImages = findViewById(R.id.recyclerImages);

        db = FirebaseFirestore.getInstance();

        recyclerImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageReviewAdapter(this, oldImageUrls);
        recyclerImages.setAdapter(imageAdapter);

        reviewDocId = getIntent().getStringExtra("review_id");
        if (TextUtils.isEmpty(reviewDocId)) {
            Toast.makeText(this, "Thiếu mã bài đánh giá", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadReviewData();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadReviewData() {
        db.collection("rate").document(reviewDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double star = doc.getDouble("star");
                        String content = doc.getString("content");

                        if (star != null) {
                            ratingBar.setRating(star.floatValue());
                            currentRating = star.floatValue();
                        }
                        edtContent.setText(content);

                        // Load ảnh cũ
                        db.collection("review_images")
                                .whereEqualTo("review_id", reviewDocId)
                                .get()
                                .addOnSuccessListener(query -> {
                                    oldImageUrls.clear();
                                    for (var d : query.getDocuments()) {
                                        String url = d.getString("img");
                                        if (url != null) oldImageUrls.add(url);
                                    }
                                    imageAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    private void saveChanges() {
        String content = edtContent.getText().toString().trim();
        float newRating = ratingBar.getRating();

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Vui lòng nhập nội dung.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference reviewRef = db.collection("rate").document(reviewDocId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("star", newRating);
        updates.put("content", content);

        reviewRef.update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã cập nhật bài đánh giá", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        for (Uri uri : newImageUris) {
            CloudinaryHelper.uploadImage(uri, this, url -> {
                if (url != null) {
                    Map<String, Object> imgData = new HashMap<>();
                    imgData.put("review_id", reviewDocId);
                    imgData.put("img", url);
                    db.collection("review_images").add(imgData);
                }
            });
        }
    }
}