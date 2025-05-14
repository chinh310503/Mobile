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
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activity.CommentActivity;
import com.example.myapplication.Activity.EditPostActivity;
import com.example.myapplication.DAO.PostDAO;
import com.example.myapplication.Model.PostModel;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.example.myapplication.Session.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

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
        ImageView ivLike, ivAvatar, ivMenu;
        ViewPager2 viewPagerImages;
        DotsIndicator dotsIndicator;

        public PostViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            viewPagerImages = itemView.findViewById(R.id.viewPagerImages);
            dotsIndicator = itemView.findViewById(R.id.dotsIndicator);
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

        if (post.timestamp > 0) {
            Date date = new Date(post.timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault());
            holder.tvTime.setText(sdf.format(date));
        } else {
            holder.tvTime.setText("Không xác định");
        }

        holder.tvLikeCount.setText(String.valueOf(post.likeCount));
        holder.tvCommentCount.setText(String.valueOf(post.commentCount));

        if (post.userAvatar != null && !post.userAvatar.isEmpty()) {
            Glide.with(context)
                    .load(post.userAvatar)
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        if (post.imageList != null && !post.imageList.isEmpty()) {
            holder.viewPagerImages.setVisibility(View.VISIBLE);

            ImagePagerAdapter adapter = new ImagePagerAdapter(context, post.imageList);
            holder.viewPagerImages.setAdapter(adapter);

            if (post.imageList.size() > 1) {
                holder.dotsIndicator.setVisibility(View.VISIBLE);
                holder.dotsIndicator.setViewPager2(holder.viewPagerImages);
            } else {
                holder.dotsIndicator.setVisibility(View.GONE);
            }
        } else {
            holder.viewPagerImages.setVisibility(View.GONE);
            holder.dotsIndicator.setVisibility(View.GONE);
        }

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

        holder.itemView.findViewById(R.id.ivComment).setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.id);
            context.startActivity(intent);
        });

        SessionManager sessionManager = new SessionManager(context);
        if (post.userId == sessionManager.getUserId()) {
            holder.ivMenu.setVisibility(View.VISIBLE);
            holder.ivMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.ivMenu);
                popup.inflate(R.menu.menu_post_options);
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        Intent intent = new Intent(context, EditPostActivity.class);
                        intent.putExtra("postId", post.id);
                        context.startActivity(intent);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        new AlertDialog.Builder(context)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc chắn muốn xóa bài viết này không?")
                                .setPositiveButton("Xóa", (dialog, which) -> {
                                    PostDAO dao = new PostDAO(context);
                                    dao.deletePostById(post.id,
                                            () -> {
                                                Toast.makeText(context, "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                                                postList.remove(holder.getAdapterPosition());
                                                notifyItemRemoved(holder.getAdapterPosition());
                                            },
                                            e -> Toast.makeText(context, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        } else {
            holder.ivMenu.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}








