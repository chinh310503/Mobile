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
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentReviewAdapter extends RecyclerView.Adapter<CommentReviewAdapter.ViewHolder> {

    private final Context context;
    private final List<CommentReviewModel> commentList;

    public CommentReviewAdapter(Context context, List<CommentReviewModel> commentList) {
        this.context = context;
        this.commentList = commentList;
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

        holder.txtUserName.setText(comment.getUserName());
        holder.txtContent.setText(comment.getContent());

        if (comment.getCreatedAt() != null) {
            Date date = comment.getCreatedAt().toDate();
            holder.txtTime.setText(getTimeAgo(date));
        } else {
            holder.txtTime.setText("Vừa xong");
        }

        if (comment.getUserAvatar() != null && !comment.getUserAvatar().isEmpty()) {
            Glide.with(context)
                    .load(comment.getUserAvatar())
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_user);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUserName, txtContent, txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtTime = itemView.findViewById(R.id.txtTime);
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
}
