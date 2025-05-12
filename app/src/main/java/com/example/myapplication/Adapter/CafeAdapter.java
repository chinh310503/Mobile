package com.example.myapplication.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.DAO.FavoriteCafeDAO;
import com.example.myapplication.R;
import com.example.myapplication.Model.CafeModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CafeAdapter extends RecyclerView.Adapter<CafeAdapter.CafeViewHolder> {
    private List<CafeModel> cafeList;
    private Context context;
    private Set<Long> favoriteCafeIds = new HashSet<>();

    public CafeAdapter(List<CafeModel> cafeList, Context context) {
        this.cafeList = cafeList;
        this.context = context;
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(CafeModel cafe, boolean isFavoriteNow);
    }

    private OnFavoriteClickListener favoriteClickListener;

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

    @NonNull
    @Override
    public CafeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cafe, parent, false);
        return new CafeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CafeViewHolder holder, int position) {
        CafeModel cafe = cafeList.get(position);
        holder.txtCafeName.setText(cafe.getName());
        holder.txtCafeAddress.setText(cafe.getAddress());
        holder.txtCafeDistance.setText("Cách bạn: " + cafe.getDistance() + " km");
        Log.d("CafeImage", "URL = " + cafe.getImg());
        Glide.with(context)
                .load(cafe.getImg())
                .placeholder(R.drawable.default_img_coffee) // ảnh mặc định khi loading
                .error(R.drawable.default_img_coffee) // ảnh khi load lỗi
                .into(holder.imgCafe);

        if (cafe.isOpen()) {
            holder.txtCafeStatus.setText("Đang mở cửa " + cafe.getOpenHours() + " - " + cafe.getCloseHours());
            holder.txtCafeStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.txtCafeStatus.setText("Đang đóng cửa " + cafe.getOpenHours() + " - " + cafe.getCloseHours());
            holder.txtCafeStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }

        boolean isFavorite = favoriteCafeIds.contains(cafe.getId());
        holder.btnFavorite.setImageResource(isFavorite ? R.drawable.red_heart : R.drawable.blank_heart);

        holder.btnFavorite.setOnClickListener(v -> {
            boolean currentFavorite = favoriteCafeIds.contains(cafe.getId());
            if (currentFavorite) {
                favoriteCafeIds.remove(cafe.getId());
                holder.btnFavorite.setImageResource(R.drawable.blank_heart);
            } else {
                favoriteCafeIds.add(cafe.getId());
                holder.btnFavorite.setImageResource(R.drawable.red_heart);
            }
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(cafe, !isFavorite);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cafeList.size();
    }

    public void setFavoriteCafes(Set<Long> favoriteIds) {
        this.favoriteCafeIds = favoriteIds;
    }
    public static class CafeViewHolder extends RecyclerView.ViewHolder {
        TextView txtCafeName, txtCafeAddress, txtCafeDistance, txtCafeStatus;
        ImageView btnFavorite, imgCafe;

        public CafeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCafeName = itemView.findViewById(R.id.txtCafeName);
            txtCafeAddress = itemView.findViewById(R.id.txtCafeAddress);
            txtCafeDistance = itemView.findViewById(R.id.txtCafeDistance);
            txtCafeStatus = itemView.findViewById(R.id.txtCafeStatus);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            imgCafe = itemView.findViewById(R.id.imgCafe);
        }
    }
}
