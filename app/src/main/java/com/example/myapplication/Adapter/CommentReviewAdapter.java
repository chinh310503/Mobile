package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.CommentReviewModel;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentReviewAdapter extends RecyclerView.Adapter<CommentReviewAdapter.ViewHolder> {

    private final Context context;
    private final List<CommentReviewModel> commentList;
    private final OnCommentInteractionListener listener;

    public CommentReviewAdapter(Context context, List<CommentReviewModel> commentList, OnCommentInteractionListener listener) {
        this.context = context;
        this.commentList = commentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentReviewModel comment = commentList.get(position);

        holder.tvUserName.setText(comment.getUserName() != null ? comment.getUserName() : "Người dùng");
        holder.tvContent.setText(comment.getContent() != null ? comment.getContent() : "");

        if (comment.getCreatedAt() != null) {
            Date date = comment.getCreatedAt().toDate();
            holder.tvTime.setText(getTimeAgo(date));
        } else {
            holder.tvTime.setText("Vừa xong");
        }

        if (comment.getUserAvatar() != null && !comment.getUserAvatar().isEmpty()) {
            Glide.with(context)
                    .load(comment.getUserAvatar())
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        // Xử lý số lượt thích (nếu có)
        holder.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));
        holder.tvCommentCount.setText(comment.getReplyCount() + " bình luận");

        // Click like
        holder.ivLike.setImageResource(comment.isLiked() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        holder.ivLike.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeComment(comment);
            }
        });

        // Click comment/reply
        holder.tvCommentCount.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReplyComment(comment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivLike;
        TextView tvUserName, tvContent, tvTime, tvLikeCount, tvCommentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivLike = itemView.findViewById(R.id.ivLike);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
        }
    }

    private String getTimeAgo(Date date) {
        long diff = System.currentTimeMillis() - date.getTime();
        long minutes = diff / (60 * 1000);
        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";
        long days = hours / 24;
        if (days < 7) return days + " ngày trước";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    // Giao diện tương tác nếu muốn xử lý like/comment/reply
    public interface OnCommentInteractionListener {
        void onLikeComment(CommentReviewModel comment);
        void onReplyComment(CommentReviewModel comment);
    }
}
