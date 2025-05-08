package com.example.myapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activity.ReviewActivity;
import com.example.myapplication.Adapter.ViewReviewAdapter;
import com.example.myapplication.DataBase.DBHandler;
import com.example.myapplication.Model.ViewReviewModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerView;
    private ViewReviewAdapter reviewAdapter;
    private List<ViewReviewModel> reviewList;
    private TextView txtAverageRating, txtReviewCount, txtCafeName;
    private RatingBar ratingBarAverage;
    private MaterialButton btnRate;

    private FirebaseFirestore db;

    private int cafeId = -1;
    private String cafeName = "";
    private int userId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.feed_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerReview);
        txtAverageRating = view.findViewById(R.id.txtAverageRating);
        txtReviewCount = view.findViewById(R.id.txtReviewCount);
        txtCafeName = view.findViewById(R.id.txtCafeName);
        ratingBarAverage = view.findViewById(R.id.ratingBarAverage);
        btnRate = view.findViewById(R.id.btnRate);

        db = DBHandler.getInstance().getDb();

        reviewList = new ArrayList<>();
        reviewAdapter = new ViewReviewAdapter(requireContext(), reviewList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(reviewAdapter);

        // Lấy cafeId từ arguments và userId từ SessionManager
        Bundle args = getArguments();
        if (args != null) {
            cafeId = (int) args.getLong("CAFE_ID", -1);
        }
        SessionManager sessionManager = new SessionManager(requireContext());
        userId = sessionManager.getUserId();

        if (cafeId != -1) {
            loadCafeInfoFromFirebase(cafeId);
        } else {
            Toast.makeText(requireContext(), "Không nhận được thông tin quán.", Toast.LENGTH_SHORT).show();
        }

        btnRate.setOnClickListener(v -> {
            if (cafeId != -1 && !cafeName.isEmpty() && userId != -1) {
                Intent intent = new Intent(requireContext(), ReviewActivity.class);
                intent.putExtra("cafe_id", cafeId);
                intent.putExtra("cafe_name", cafeName);
                intent.putExtra("id_user", userId);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Đang tải thông tin quán hoặc người dùng...", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadCafeInfoFromFirebase(int cafeId) {
        db.collection("cafes")
                .whereEqualTo("id", cafeId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        this.cafeId = doc.getLong("id").intValue();
                        this.cafeName = doc.getString("name");

                        txtCafeName.setText(cafeName);

                        loadReviews();
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy quán.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi tải quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReviews() {
        if (cafeId == -1) return;

        db.collection("rate")
                .whereEqualTo("id_cafe", cafeId)
                .get()
                .addOnSuccessListener(rateSnapshot -> {
                    reviewList.clear();

                    if (!rateSnapshot.isEmpty()) {
                        AtomicInteger loadedUsers = new AtomicInteger(0);
                        int total = rateSnapshot.size();

                        for (QueryDocumentSnapshot rateDoc : rateSnapshot) {
                            String comment = rateDoc.getString("comment");
                            Long starLong = rateDoc.getLong("star");
                            int star = starLong != null ? starLong.intValue() : 0;
                            Long idUserLong = rateDoc.getLong("id_user");
                            int idUser = idUserLong != null ? idUserLong.intValue() : -1;
                            Timestamp createdAt = rateDoc.getTimestamp("created_at");

                            db.collection("users")
                                    .whereEqualTo("id", idUser)
                                    .get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        String userName = "Ẩn danh";
                                        String userImg = "";

                                        if (!userSnapshot.isEmpty()) {
                                            DocumentSnapshot userDoc = userSnapshot.getDocuments().get(0);
                                            userName = userDoc.getString("name");
                                            userImg = userDoc.getString("img");
                                        }

                                        ViewReviewModel review = new ViewReviewModel();
                                        review.setUserName(userName);
                                        review.setUserAvatar(userImg);
                                        review.setCreatedAt(createdAt);
                                        review.setContent(comment);
                                        review.setRating((float) star);
                                        review.setImageUrls(Collections.emptyList());
                                        review.setLikeCount(0);
                                        review.setCommentCount(0);

                                        reviewList.add(review);

                                        if (loadedUsers.incrementAndGet() == total) {
                                            updateSummaryUI();
                                            reviewAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> loadedUsers.incrementAndGet());
                        }
                    } else {
                        txtAverageRating.setText("0.0");
                        txtReviewCount.setText("0 đánh giá");
                        ratingBarAverage.setRating(0);
                        reviewAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi tải đánh giá", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateSummaryUI() {
        float totalStars = 0f;
        int totalReviews = reviewList.size();

        for (ViewReviewModel review : reviewList) {
            totalStars += review.getRating();
        }

        float average = totalReviews > 0 ? totalStars / totalReviews : 0f;

        txtAverageRating.setText(String.format("%.1f", average));
        txtReviewCount.setText(totalReviews + " đánh giá");
        ratingBarAverage.setRating(average);
    }
}