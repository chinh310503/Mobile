package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.CommentReviewModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentReviewAdapter extends RecyclerView.Adapter<CommentReviewAdapter.ViewHolder> {

    private final Context context;
    private final List<CommentReviewModel> commentList;
    private final OnCommentInteractionListener listener;
    private final SessionManager sessionManager;

    public CommentReviewAdapter(Context context, List<CommentReviewModel> commentList, OnCommentInteractionListener listener) {
        this.context = context;
        this.commentList = commentList;
        this.listener = listener;
        this.sessionManager = new SessionManager(context);
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

        holder.ivMenu.setVisibility(View.VISIBLE);
        holder.ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.ivMenu);
            MenuInflater inflater = popup.getMenuInflater();
            if (comment.getUserId() == sessionManager.getUserId()) {
                inflater.inflate(R.menu.menu_review_owner, popup.getMenu());
            } else {
                inflater.inflate(R.menu.menu_review_other, popup.getMenu());
            }

            popup.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;

                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    listener.onEditComment(comment, position);
                    return true;
                } else if (id == R.id.action_delete) {
                    listener.onDeleteComment(comment, position);
                    return true;
                } else if (id == R.id.action_report) {
                    listener.onReportComment(comment, position);
                    return true;
                }

                return false;
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivMenu;
        TextView tvUserName, tvContent, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
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

    public interface OnCommentInteractionListener {
        void onEditComment(CommentReviewModel comment, int position);
        void onDeleteComment(CommentReviewModel comment, int position);
        void onReportComment(CommentReviewModel comment, int position);
    }
}
