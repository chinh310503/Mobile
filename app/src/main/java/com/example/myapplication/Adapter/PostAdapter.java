package com.example.myapplication.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.Model.PostModel;
import com.example.myapplication.R;

import java.util.List;
import android.content.SharedPreferences;
import android.widget.Toast;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<PostModel> postList;

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
        holder.tvUserName.setText(post.userName);
        holder.tvContent.setText(post.content);
        holder.tvTime.setText(post.timePosted);
        holder.tvLikeCount.setText(String.valueOf(post.likeCount));
        holder.tvCommentCount.setText(String.valueOf(post.commentCount));

        if (post.isLiked) {
            holder.ivLike.setImageResource(R.drawable.ic_heart_filled); // ❤️
        } else {
            holder.ivLike.setImageResource(R.drawable.ic_heart_outline); // 🤍
        }
        //Su kien an nut Like
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
            // Update trạng thái local
            if (post.isLiked) {
                post.likeCount++;
            } else {
                if (post.likeCount > 0) {
                    post.likeCount--;
                }
            }

            notifyItemChanged(holder.getAdapterPosition());
        });
        if (post.avatarUri != null && !post.avatarUri.isEmpty()) {
            holder.ivAvatar.setImageURI(Uri.parse(post.avatarUri));
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user); // avatar mặc định
        }

        // Xóa ảnh cũ (nếu có)
        holder.llImages.removeAllViews();
        if (post.imageList != null && !post.imageList.isEmpty()) {
            holder.llImages.setVisibility(View.VISIBLE); // Hiện ảnh nếu có

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
            holder.llImages.setVisibility(View.GONE); // Ẩn nếu không có ảnh
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
