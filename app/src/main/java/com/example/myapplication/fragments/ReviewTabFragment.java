package com.example.myapplication.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.ReviewModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.myapplication.Adapter.ReviewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReviewTabFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList = new ArrayList<>();
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_tab, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        reviewAdapter = new ReviewAdapter(reviewList, requireContext());
        recyclerView.setAdapter(reviewAdapter);

        sessionManager = new SessionManager(requireContext());
        db = FirebaseFirestore.getInstance();

        int userId = sessionManager.getUserId();
        if (userId != -1) {
            db.collection("rate")
                    .whereEqualTo("id_user", userId)
                    .orderBy("created_at", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(query -> {
                        reviewList.clear();
                        for (QueryDocumentSnapshot doc : query) {
                            ReviewModel review = new ReviewModel();

                            review.setContent(doc.getString("comment"));
                            review.setRating(doc.getLong("star").intValue());
                            review.setCreatedAt(doc.getTimestamp("created_at").toDate().getTime());
                            review.setUserId(doc.getLong("id_user").intValue());
                            review.setCafeId(doc.getLong("id_cafe").intValue());

                            review.setUserName(sessionManager.getUserName());
                            review.setUserAvatar(sessionManager.getUserImg());

                            db.collection("cafes")
                                    .whereEqualTo("id", review.getCafeId())
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(cafeQuery -> {
                                        if (!cafeQuery.isEmpty()) {
                                            String cafeName = cafeQuery.getDocuments().get(0).getString("name");
                                            review.setCafeName(cafeName);
                                            Log.d("cafe name", review.getCafeName());
                                        } else {
                                            review.setCafeName("PTIT Coffee");
                                        }
                                        reviewList.add(review);
                                        reviewAdapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Log.e("ReviewTab", "Lỗi khi lấy tên quán", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ReviewTab", "Lỗi khi lấy đánh giá", e));
        }

        return view;
    }
}

