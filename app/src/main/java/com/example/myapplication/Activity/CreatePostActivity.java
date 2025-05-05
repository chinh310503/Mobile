package com.example.myapplication.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import com.example.myapplication.Session.SessionManager;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etContent;
    private Button btnSelectImages, btnPost;
    private LinearLayout llPreviewImages;
    private List<Uri> selectedImages = new ArrayList<>();
    private static final int REQUEST_SELECT_IMAGES = 101;

    private PostDAO postDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        etContent = findViewById(R.id.etContent);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnPost = findViewById(R.id.btnPost);
        llPreviewImages = findViewById(R.id.llPreviewImages);

        postDAO = new PostDAO();

        btnSelectImages.setOnClickListener(v -> openImagePicker());

        btnPost.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Bạn chưa nhập nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy userId từ SessionManager
            SessionManager sessionManager = new SessionManager(this);
            int currentUserId = sessionManager.getUserId();
            if (currentUserId == -1) {
                Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chèn bài viết mới vào Firebase
            PostDAO postDAO = new PostDAO(this); // Truyền context vào đây
            postDAO.insertPost(
                    CreatePostActivity.this, // truyền Context
                    currentUserId,
                    content,
                    selectedImages,
                    unused -> {
                        Toast.makeText(CreatePostActivity.this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreatePostActivity.this, MainActivity.class);
                        intent.putExtra("isNewPost", true);
                        startActivity(intent);
                        finish();
                    },
                    e -> Toast.makeText(CreatePostActivity.this, "Lỗi khi đăng bài: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );

        });


    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), REQUEST_SELECT_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGES && resultCode == RESULT_OK) {
            if (data != null) {
                llPreviewImages.removeAllViews();
                selectedImages.clear();

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        getContentResolver().takePersistableUriPermission(
                                imageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        selectedImages.add(imageUri);
                        addImageToPreview(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    getContentResolver().takePersistableUriPermission(
                            imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    selectedImages.add(imageUri);
                    addImageToPreview(imageUri);
                }
            }
        }
    }

    private void addImageToPreview(Uri uri) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(uri);
        llPreviewImages.addView(imageView);
    }
}

