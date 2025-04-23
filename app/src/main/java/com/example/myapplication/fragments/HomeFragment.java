package com.example.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activity.CreatePostActivity;
import com.example.myapplication.Adapter.PostAdapter;
import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.Model.PostModel;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvPosts;
    private PostDAO postDAO;
    private List<PostModel> postList;
    private PostAdapter adapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);

        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        postDAO = new PostDAO();
        postList = new ArrayList<>();
        adapter = new PostAdapter(getContext(), postList);
        rvPosts.setAdapter(adapter);

        TextView tvCreatePost = view.findViewById(R.id.tvCreatePost);
        tvCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
            startActivity(intent);
        });

        loadPosts();

        return view;
    }

    private void loadPosts() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

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
        loadPosts();
    }
}