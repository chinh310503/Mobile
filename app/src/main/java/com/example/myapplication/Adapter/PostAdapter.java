package com.example.myapplication.Adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.Model.PostModel;
import com.example.myapplication.R;

import java.util.List;
import com.example.myapplication.Session.SessionManager;
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
        LinearLayout llImages;

        public PostViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivLike = itemView.findViewById(R.id.ivLike);
            llImages = itemView.findViewById(R.id.llImages);
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
                    .placeholder(R.drawable.ic_user) // icon user mặc định
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        // Ảnh bài viết
        holder.llImages.removeAllViews();
        if (post.imageList != null && !post.imageList.isEmpty()) {
            holder.llImages.setVisibility(View.VISIBLE);
            for (String imagePath : post.imageList) {
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // Load ảnh từ URL bằng Glide
                Glide.with(context)
                        .load(imagePath)
                        .placeholder(R.drawable.ic_placeholder) // icon loading
                        .error(R.drawable.ic_error_image)       // icon lỗi
                        .into(imageView);

                holder.llImages.addView(imageView);
            }
        } else {
            holder.llImages.setVisibility(View.GONE);
        }

        if (post.isLiked) {
            holder.ivLike.setImageResource(R.drawable.ic_heart_filled); // đã like (❤️)
        } else {
            holder.ivLike.setImageResource(R.drawable.ic_heart_outline); // chưa like (🤍)
        }

        // Sự kiện click vào nút like
        holder.ivLike.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(context);
            int userId = sessionManager.getUserId();
            if (userId == -1) {
                Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            PostDAO dao = new PostDAO();
            dao.toggleLike(post.id, userId);

            // Lật trạng thái like và update UI
            post.isLiked = !post.isLiked;
            if (post.isLiked) {
                post.likeCount++;
            } else {
                if (post.likeCount > 0) {
                    post.likeCount--;
                }
            }
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}

