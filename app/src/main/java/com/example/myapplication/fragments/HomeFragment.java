package com.example.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activity.CreatePostActivity;
import com.example.myapplication.Activity.LoginActivity;
import com.example.myapplication.Adapter.PostAdapter;
import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.Model.PostModel;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import com.example.myapplication.Session.SessionManager;

public class HomeFragment extends Fragment {

    private RecyclerView rvPosts;
    private PostDAO postDAO;
    private List<PostModel> postList;
    private PostAdapter adapter;
    private SessionManager sessionManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);

        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        sessionManager = new SessionManager(getContext()); // Khởi tạo SessionManager
        postDAO = new PostDAO();
        postList = new ArrayList<>();
        adapter = new PostAdapter(getContext(), postList);
        rvPosts.setAdapter(adapter);

        // Chuyển sang màn hình tạo bài viết
        TextView tvCreatePost = view.findViewById(R.id.tvCreatePost);
        tvCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            startActivity(intent);
        });

        loadPosts(); // Load bài viết

        return view;
    }

    private void loadPosts() {
        // Lấy userId từ SessionManager
        int userId = sessionManager.getUserId(); // Lấy user_id từ session
        Log.d("HomeFragment", "Current userId: " + userId);
        // Nếu userId là -1 (chưa đăng nhập), thông báo lỗi hoặc điều hướng đến màn hình đăng nhập
        if (userId == -1) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem bài viết!", Toast.LENGTH_SHORT).show();
            // Có thể điều hướng đến LoginActivity nếu chưa đăng nhập
            startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }

        postDAO.getAllPosts(userId, new PostDAO.PostListCallback() {
            @Override
            public void onSuccess(List<PostModel> posts) {
                postList.clear();
                postList.addAll(posts);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Lỗi tải bài viết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPosts(); // Tải lại bài viết khi quay lại fragment
    }
}


