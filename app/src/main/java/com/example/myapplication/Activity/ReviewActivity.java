package com.example.myapplication.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.ImageAdapter;
import com.example.myapplication.DAO.ReviewDAO;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private RatingBar ratingBar;
    private EditText edtComment;
    private RecyclerView rvImages;
    private Button btnAddImage, btnSubmit;

    private List<Uri> imageUriList;
    private ImageAdapter imageAdapter;

    private ReviewDAO reviewDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtComment);
        rvImages = findViewById(R.id.rvImages);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSubmit = findViewById(R.id.btnSubmit);

        reviewDAO = new ReviewDAO();

        imageUriList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageUriList, this);
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);

        btnAddImage.setOnClickListener(v -> openImagePicker());

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = edtComment.getText().toString().trim();

            if (rating == 0 || comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            reviewDAO.uploadReview(this, rating, comment, imageUriList, new ReviewDAO.OnUploadReviewListener() {
                @Override
                public void onComplete(boolean success) {
                    if (success) {
                        Toast.makeText(ReviewActivity.this, "Đánh giá đã được gửi!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ReviewActivity.this, "Lỗi khi gửi đánh giá!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUriList.add(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUriList.add(imageUri);
            }
            imageAdapter.notifyDataSetChanged();
        }
    }
}
