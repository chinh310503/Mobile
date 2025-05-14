package com.example.myapplication.Activity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.Adapter.ImagePagerAdapter;
import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class EditPostActivity extends AppCompatActivity {

    private EditText edtContent;
    private ViewPager2 viewPager;
    private WormDotsIndicator dotsIndicator;
    private Button btnSave, btnAddImages;
    private int postId;
    private List<String> currentImages = new ArrayList<>();
    private List<Uri> newImageUris = new ArrayList<>();
    private ImagePagerAdapter imagePagerAdapter;
    private PostDAO postDAO;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        ClipData clipData = result.getData().getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            newImageUris.add(uri);
                        }
                    } else if (result.getData().getData() != null) {
                        newImageUris.add(result.getData().getData());
                    }
                    updateImagePreview();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        edtContent = findViewById(R.id.edtContent);
        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = (WormDotsIndicator) findViewById(R.id.dotsIndicator);
        btnSave = findViewById(R.id.btnSave);
        btnAddImages = findViewById(R.id.btnAddImages);

        db = FirebaseFirestore.getInstance();
        postDAO = new PostDAO(this);

        postId = getIntent().getIntExtra("postId", -1);

        imagePagerAdapter = new ImagePagerAdapter(this, currentImages, null);
        viewPager.setAdapter(imagePagerAdapter);
        dotsIndicator.setViewPager2(viewPager);

        btnAddImages.setOnClickListener(v -> pickImages());
        btnSave.setOnClickListener(v -> saveChanges());

        loadPost();
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadPost() {
        db.collection("post")
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        edtContent.setText(doc.getString("content"));
                    }
                });

        db.collection("post_image")
                .whereEqualTo("id_post", postId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    currentImages.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        currentImages.add(doc.getString("img"));
                    }
                    updateImagePreview();
                });
    }

    private void updateImagePreview() {
        List<String> allImages = new ArrayList<>(currentImages);
        for (Uri uri : newImageUris) {
            allImages.add(uri.toString());
        }
        imagePagerAdapter = new ImagePagerAdapter(this, allImages, new ImagePagerAdapter.OnImageRemoveListener() {
            @Override
            public void onRemove(String imageUrl) {
                int position = allImages.indexOf(imageUrl);
                if (position < currentImages.size()) {
                    currentImages.remove(position);
                } else {
                    int uriIndex = position - currentImages.size();
                    if (uriIndex >= 0 && uriIndex < newImageUris.size()) {
                        newImageUris.remove(uriIndex);
                    }
                }
                updateImagePreview();
            }
        });
        viewPager.setAdapter(imagePagerAdapter);
        if (allImages.size() > 1) {
            dotsIndicator.setVisibility(View.VISIBLE);
            dotsIndicator.setViewPager2(viewPager);
        } else {
            dotsIndicator.setVisibility(View.GONE);
        }
    }

    private void saveChanges() {
        String newContent = edtContent.getText().toString().trim();
        if (newContent.isEmpty()) {
            Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("post")
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentReference docRef = snapshot.getDocuments().get(0).getReference();
                        docRef.update("content", newContent);
                    }
                });

        db.collection("post_image")
                .whereEqualTo("id_post", postId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        if (!currentImages.contains(doc.getString("img"))) {
                            doc.getReference().delete();
                        }
                    }
                    if (!newImageUris.isEmpty()) {
                        postDAO.uploadImages(this, postId, newImageUris,
                                unused -> finish(),
                                e -> Toast.makeText(this, "Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        finish();
                    }
                });
    }
}
