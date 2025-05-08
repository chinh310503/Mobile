package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.Model.PostModel;
import com.example.myapplication.R;

import java.util.List;

public class PostReviewAdapter extends RecyclerView.Adapter<PostReviewAdapter.PostReviewViewHolder> {

    private final Context context;
    private final List<PostModel> postList;

    public PostReviewAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    public static class PostReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvContent, tvTime, tvLikeCount, tvCommentCount;
        ImageView ivLike, ivAvatar;
        LinearLayout llImages;

        public PostReviewViewHolder(View itemView) {
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
    public PostReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostReviewViewHolder holder, int position) {
        PostModel post = postList.get(position);
        holder.tvUserName.setText(post.userName);
        holder.tvContent.setText(post.content);
        holder.tvTime.setText(post.timePosted);
        holder.tvLikeCount.setText(String.valueOf(post.likeCount));
        holder.tvCommentCount.setText(String.valueOf(post.commentCount));

        // Set trạng thái like
        if (post.isLiked) {
            holder.ivLike.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.ivLike.setImageResource(R.drawable.ic_heart_outline);
        }

        // Xử lý sự kiện Like
        holder.ivLike.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            PostDAO dao = new PostDAO();
            dao.toggleLike(post.id, userId);
            post.isLiked = !post.isLiked;

            if (post.isLiked) {
                post.likeCount++;
            } else if (post.likeCount > 0) {
                post.likeCount--;
            }

            notifyItemChanged(holder.getAdapterPosition());
        });

        // Avatar
        if (post.avatarUri != null && !post.avatarUri.isEmpty()) {
            holder.ivAvatar.setImageURI(Uri.parse(post.avatarUri));
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        // Ảnh trong bài viết
        holder.llImages.removeAllViews();
        if (post.imageList != null && !post.imageList.isEmpty()) {
            holder.llImages.setVisibility(View.VISIBLE);
            for (String imagePath : post.imageList) {
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageURI(Uri.parse(imagePath));
                holder.llImages.addView(imageView);
            }
        } else {
            holder.llImages.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
