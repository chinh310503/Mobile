package com.example.myapplication.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.Adapter.ImagePagerAdapter;
import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EditPostActivity extends AppCompatActivity {

    private EditText edtContent;
    private ViewPager2 viewPagerImages;
    private Button btnSave, btnChooseImages;
    private int postId;
    private List<String> imageList = new ArrayList<>();
    private List<Uri> newImageUris = new ArrayList<>();
    private FirebaseFirestore db;
    private PostDAO postDAO;
    private ImagePagerAdapter imagePagerAdapter;
    private static final int REQUEST_CODE_PICK_IMAGES = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        edtContent = findViewById(R.id.edtContent);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        btnSave = findViewById(R.id.btnSave);
        btnChooseImages = findViewById(R.id.btnChooseImages);

        db = FirebaseFirestore.getInstance();
        postDAO = new PostDAO(this);
        postId = getIntent().getIntExtra("postId", -1);

        imagePagerAdapter = new ImagePagerAdapter(this, imageList, uri -> {
            imageList.remove(uri);
            imagePagerAdapter.notifyDataSetChanged();
        });
        viewPagerImages.setAdapter(imagePagerAdapter);

        loadPostData();

        btnSave.setOnClickListener(v -> saveChanges());
        btnChooseImages.setOnClickListener(v -> pickImagesFromGallery());
    }

    private void loadPostData() {
        db.collection("post")
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        var doc = snapshot.getDocuments().get(0);
                        String content = doc.getString("content");
                        edtContent.setText(content);

                        db.collection("post_image")
                                .whereEqualTo("id_post", postId)
                                .get()
                                .addOnSuccessListener(imgSnap -> {
                                    imageList.clear();
                                    for (var imgDoc : imgSnap) {
                                        String img = imgDoc.getString("img");
                                        imageList.add(img);
                                    }
                                    imagePagerAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    private void pickImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), REQUEST_CODE_PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    newImageUris.add(uri);
                    imageList.add(uri.toString());
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                newImageUris.add(uri);
                imageList.add(uri.toString());
            }
            imagePagerAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã chọn " + newImageUris.size() + " ảnh mới", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        String updatedContent = edtContent.getText().toString().trim();
        if (updatedContent.isEmpty()) {
            Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("post")
                .whereEqualTo("id", postId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (var doc : snapshot) {
                        doc.getReference().update("content", updatedContent);
                    }

                    db.collection("post_image")
                            .whereEqualTo("id_post", postId)
                            .get()
                            .addOnSuccessListener(oldSnap -> {
                                for (var doc : oldSnap) {
                                    String oldUrl = doc.getString("img");
                                    if (!imageList.contains(oldUrl)) {
                                        doc.getReference().delete();
                                    }
                                }

                                if (!newImageUris.isEmpty()) {
                                    postDAO.uploadImages(this, postId, newImageUris,
                                            unused -> {
                                                Toast.makeText(this, "Đã cập nhật bài viết", Toast.LENGTH_SHORT).show();
                                                finish();
                                            },
                                            e -> Toast.makeText(this, "Lỗi khi upload ảnh", Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(this, "Đã cập nhật nội dung", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                });
    }
}


