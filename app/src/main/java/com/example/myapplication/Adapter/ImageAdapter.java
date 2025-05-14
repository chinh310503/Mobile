package com.example.myapplication.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Uri> imageUriList;
    private Context context;

    public ImageAdapter(List<Uri> imageUriList, Context context) {
        this.imageUriList = imageUriList;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUriList.get(position);
        Glide.with(context)
                .load(imageUri)
                .into(holder.imgPhoto);
    }

    @Override
    public int getItemCount() {
        return imageUriList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }
}
