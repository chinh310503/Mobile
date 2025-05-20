package com.example.myapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.myapplication.DAO.ReviewDAO;
import com.example.myapplication.Model.ViewReviewModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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
    private ReviewDAO reviewDAO;
    private SessionManager sessionManager;

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

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(requireContext());
        reviewDAO = new ReviewDAO();
        reviewList = new ArrayList<>();

        reviewAdapter = new ViewReviewAdapter(requireContext(), reviewList, new ViewReviewAdapter.OnInteractionListener() {
            @Override
            public void onLikeClicked(ViewReviewModel review) {
                reviewDAO.toggleLike(review.getId(), sessionManager.getUserId());
                review.setLiked(!review.isLiked());
                if (review.isLiked()) {
                    review.setLikeCount(review.getLikeCount() + 1);
                } else {
                    review.setLikeCount(Math.max(0, review.getLikeCount() - 1));
                }
                reviewAdapter.notifyItemChanged(reviewList.indexOf(review));
            }

            @Override
            public void onEditClicked(ViewReviewModel review) {
                Toast.makeText(requireContext(), "Chức năng chỉnh sửa được triển khai", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClicked(ViewReviewModel review) {
                Toast.makeText(requireContext(), "Chức năng xoá được triển khai", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReportClicked(ViewReviewModel review) {
                Toast.makeText(requireContext(), "Báo cáo đánh giá thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public int getCurrentUserId() {
                return sessionManager.getUserId();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(reviewAdapter);

        userId = sessionManager.getUserId();

        Bundle args = getArguments();
        if (args != null) {
            cafeId = args.getInt("cafe_id", -1);
            cafeName = args.getString("cafe_name", "");
        }

        Log.d("FeedFragment", "cafeId = " + cafeId + ", cafeName = " + cafeName + ", userId = " + userId);

        if (cafeId == -1) {
            Toast.makeText(requireContext(), "Không nhận được thông tin quán.", Toast.LENGTH_SHORT).show();
        } else {
            if (cafeName == null || cafeName.isEmpty()) {
                loadCafeNameFromFirebase(cafeId);
            } else {
                txtCafeName.setText(cafeName);
            }
            loadReviews();
        }

        btnRate.setOnClickListener(v -> {
            Log.d("FeedFragment", "btnRate click - cafeId: " + cafeId + ", userId: " + userId);
            if (cafeId != -1 && userId != -1) {
                sessionManager.setInt("selected_cafe_id", cafeId);
                Intent intent = new Intent(requireContext(), ReviewActivity.class);
                intent.putExtra("cafe_id", cafeId);
                intent.putExtra("cafe_name", cafeName);
                intent.putExtra("id_user", userId);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Chưa sẵn sàng để đánh giá.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cafeId != -1 && userId != -1) {
            loadReviews();
        }
    }

    private void loadCafeNameFromFirebase(int cafeId) {
        db.collection("cafes")
                .whereEqualTo("id", cafeId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        cafeName = doc.getString("name");
                        txtCafeName.setText(cafeName);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi tải tên quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReviews() {
        reviewDAO.getReviewsByCafeIdRealtime(cafeId, userId, new ReviewDAO.ReviewCallback() {
            @Override
            public void onSuccess(List<ViewReviewModel> list) {
                reviewList.clear();
                reviewList.addAll(list);
                updateSummaryUI();
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Lỗi tải đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
