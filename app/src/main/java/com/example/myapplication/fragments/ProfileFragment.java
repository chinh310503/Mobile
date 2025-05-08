package com.example.myapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activity.LoginActivity;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.example.myapplication.Adapter.ProfilePagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileFragment extends Fragment {

    private TextView userNameTextView;
    private ImageView avatarImageView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        userNameTextView = view.findViewById(R.id.user_name);
        avatarImageView = view.findViewById(R.id.user_avatar);
        tabLayout = view.findViewById(R.id.profile_tabs);
        viewPager = view.findViewById(R.id.profile_viewpager);
        viewPager.setSaveEnabled(false);

        sessionManager = new SessionManager(requireContext());
        userNameTextView.setText(sessionManager.getUserName());
        Glide.with(requireContext())
                .load(R.drawable.avatar)
                .circleCrop()
                .into(avatarImageView);

        ProfilePagerAdapter pagerAdapter = new ProfilePagerAdapter(getChildFragmentManager(), getLifecycle());
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Reviews"); break;
                case 1: tab.setText("Yêu thích"); break;
            }
        }).attach();

        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa hết stack
            startActivity(intent);
        });

        return view;
    }
}
