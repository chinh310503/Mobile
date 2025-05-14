package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activity.CommentReviewActivity;
import com.example.myapplication.Model.ViewReviewModel;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewReviewAdapter extends RecyclerView.Adapter<ViewReviewAdapter.ViewHolder> {

    private final Context context;
    private final List<ViewReviewModel> reviewList;
    private final OnInteractionListener listener;

    public ViewReviewAdapter(Context context, List<ViewReviewModel> reviewList, OnInteractionListener listener) {
        this.context = context;
        this.reviewList = reviewList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewReviewModel review = reviewList.get(position);

        holder.txtUserName.setText(review.getUserName());
        holder.txtContent.setText(review.getContent());
        holder.ratingBar.setRating(review.getRating());
        holder.txtLikeCount.setText(String.valueOf(review.getLikeCount()));
        holder.txtCommentCount.setText(String.valueOf(review.getComments()));

        if (review.getCreatedAt() != null) {
            Date date = review.getCreatedAt().toDate();
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
            holder.txtTime.setText(formattedDate);
        } else {
            holder.txtTime.setText("Không rõ thời gian");
        }

        if (review.getUserAvatar() != null && !review.getUserAvatar().isEmpty()) {
            Glide.with(context).load(review.getUserAvatar()).into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.default_avatar);
        }

        if (review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
            holder.recyclerImages.setVisibility(View.VISIBLE);
            holder.recyclerImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            ImageReviewAdapter imageAdapter = new ImageReviewAdapter(context, review.getImageUrls());
            holder.recyclerImages.setAdapter(imageAdapter);
        } else {
            holder.recyclerImages.setVisibility(View.GONE);
        }

        holder.btnLike.setImageResource(review.isLiked() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        holder.btnLike.setOnClickListener(v -> {
            if (listener != null) listener.onLikeClicked(review);
        });

        holder.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentReviewActivity.class);
            intent.putExtra("review_id", review.getId());
            context.startActivity(intent);
        });

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMore);
            MenuInflater inflater = popup.getMenuInflater();
            if (review.getUserId() == listener.getCurrentUserId()) {
                inflater.inflate(R.menu.menu_review_owner, popup.getMenu());
            } else {
                inflater.inflate(R.menu.menu_review_other, popup.getMenu());
            }

            popup.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    listener.onEditClicked(review);
                    return true;
                } else if (id == R.id.action_delete) {
                    listener.onDeleteClicked(review);
                    return true;
                } else if (id == R.id.action_report) {
                    listener.onReportClicked(review);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnMore, btnLike, btnComment;
        TextView txtUserName, txtTime, txtContent, txtLikeCount, txtCommentCount;
        RatingBar ratingBar;
        RecyclerView recyclerImages;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            txtCommentCount = itemView.findViewById(R.id.txtCommentCount);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            recyclerImages = itemView.findViewById(R.id.recyclerImages);
        }
    }

    public interface OnInteractionListener {
        void onLikeClicked(ViewReviewModel review);
        void onEditClicked(ViewReviewModel review);
        void onDeleteClicked(ViewReviewModel review);
        void onReportClicked(ViewReviewModel review);
        int getCurrentUserId();
    }
}
