package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.Model.PostModel;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import com.example.myapplication.Session.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.widget.Toast;
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final List<PostModel> postList;

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvContent, tvTime, tvLikeCount, tvCommentCount;
        ImageView ivLike, ivAvatar;
        ViewPager2 viewPagerImages;

        public PostViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivLike = itemView.findViewById(R.id.ivLike);
            viewPagerImages = itemView.findViewById(R.id.viewPagerImages);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostModel post = postList.get(position);

        holder.tvUserName.setText(post.userName != null ? post.userName : "User");
        holder.tvContent.setText(post.content != null ? post.content : "");
        holder.tvTime.setText(post.timePosted != null ? post.timePosted : "");
        holder.tvLikeCount.setText(String.valueOf(post.likeCount));
        holder.tvCommentCount.setText(String.valueOf(post.commentCount));

        // Avatar
        if (post.userAvatar != null && !post.userAvatar.isEmpty()) {
            Glide.with(context)
                    .load(post.userAvatar)
                    .placeholder(R.drawable.ic_user)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        // Ảnh bài viết bằng ViewPager2
        if (post.imageList != null && !post.imageList.isEmpty()) {
            holder.viewPagerImages.setVisibility(View.VISIBLE);
            ImagePagerAdapter adapter = new ImagePagerAdapter(context, post.imageList);
            holder.viewPagerImages.setAdapter(adapter);
        } else {
            holder.viewPagerImages.setVisibility(View.GONE);
        }

        // Like
        holder.ivLike.setImageResource(post.isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        holder.ivLike.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(context);
            int userId = sessionManager.getUserId();
            if (userId == -1) {
                Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            PostDAO dao = new PostDAO();
            dao.toggleLike(post.id, userId);

            post.isLiked = !post.isLiked;
            post.likeCount += post.isLiked ? 1 : -1;
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}





