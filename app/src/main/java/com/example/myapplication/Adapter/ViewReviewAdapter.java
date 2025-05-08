package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.ViewReviewModel;
import com.example.myapplication.R;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ViewReviewAdapter extends RecyclerView.Adapter<ViewReviewAdapter.ViewReviewViewHolder> {

    private final Context context;
    private final List<ViewReviewModel> reviewList;

    public ViewReviewAdapter(Context context, List<ViewReviewModel> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_item_review, parent, false);
        return new ViewReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewReviewViewHolder holder, int position) {
        ViewReviewModel review = reviewList.get(position);

        holder.txtUserName.setText(review.getUserName());
        holder.txtContent.setText(review.getContent());
        holder.ratingBar.setRating(review.getStar());
        holder.txtLikeCount.setText(String.valueOf(review.getLikeCount()));
        holder.txtCommentCount.setText(String.valueOf(review.getCommentCount()));

        // Avatar
        if (review.getUserAvatar() != null && !review.getUserAvatar().isEmpty()) {
            Glide.with(context)
                    .load(review.getUserAvatar())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.default_avatar);
        }

        // Time ago
        if (review.getCreatedAt() != null) {
            holder.txtCreatedAt.setText(formatTimeAgo(review.getCreatedAt().toDate()));
        } else {
            holder.txtCreatedAt.setText("Chưa rõ");
        }

        // Ảnh review
        if (review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
            holder.recyclerImages.setVisibility(View.VISIBLE);
            holder.recyclerImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerImages.setHasFixedSize(true);
            holder.recyclerImages.setAdapter(new ImageReviewAdapter(context, review.getImageUrls()));
        } else {
            holder.recyclerImages.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUserName, txtCreatedAt, txtContent, txtLikeCount, txtCommentCount;
        RatingBar ratingBar;
        RecyclerView recyclerImages;

        public ViewReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtCreatedAt = itemView.findViewById(R.id.txtCreatedAt);
            txtContent = itemView.findViewById(R.id.txtContent);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            txtCommentCount = itemView.findViewById(R.id.txtCommentCount);
            recyclerImages = itemView.findViewById(R.id.recyclerImages);
        }
    }

    private String formatTimeAgo(Date date) {
        long diffMillis = new Date().getTime() - date.getTime();
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (diffDays < 1) return "Hôm nay";
        else if (diffDays == 1) return "Hôm qua";
        else if (diffDays < 30) return diffDays + " ngày trước";
        else if (diffDays < 365) return (diffDays / 30) + " tháng trước";
        else return (diffDays / 365) + " năm trước";
    }
}
