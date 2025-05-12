package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.PagerViewHolder> {

    public interface OnImageRemoveListener {
        void onRemove(String uri);
    }

    private final Context context;
    private final List<String> imageUrls;
    private final OnImageRemoveListener removeListener;

    public ImagePagerAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.removeListener = null;
    }

    public ImagePagerAdapter(Context context, List<String> imageUrls, OnImageRemoveListener listener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_pager, parent, false);
        return new PagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error_image)
                .into(holder.imageView);

        if (removeListener != null) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> removeListener.onRemove(imageUrl));
        } else {
            holder.btnRemove.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class PagerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView btnRemove;

        PagerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagePager);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}




