package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Model.CafeModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CafeAdapter extends RecyclerView.Adapter<CafeAdapter.CafeViewHolder> {
    private List<CafeModel> cafeList;
    private Context context;
    private Set<String> favoriteCafes = new HashSet<>(); // Danh sách quán yêu thích

    public CafeAdapter(List<CafeModel> cafeList, Context context) {
        this.cafeList = cafeList;
        this.context = context;
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

        if (cafe.isOpen()) {
            holder.txtCafeStatus.setText("Mở cửa " + cafe.getOpenHours() + " - " + cafe.getCloseHours());
            holder.txtCafeStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.txtCafeStatus.setText("Đóng cửa " + cafe.getOpenHours() + " - " + cafe.getCloseHours());
            holder.txtCafeStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }

        // Kiểm tra quán có trong danh sách yêu thích không
        if (favoriteCafes.contains(cafe.getName())) {
            holder.btnFavorite.setImageResource(R.drawable.red_heart); // Biểu tượng tym đã chọn
        } else {
            holder.btnFavorite.setImageResource(R.drawable.blank_heart); // Biểu tượng tym trống
        }

        // Xử lý khi nhấn vào nút tym
        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteCafes.contains(cafe.getName())) {
                favoriteCafes.remove(cafe.getName());
                holder.btnFavorite.setImageResource(R.drawable.red_heart);
            } else {
                favoriteCafes.add(cafe.getName());
                holder.btnFavorite.setImageResource(R.drawable.blank_heart);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cafeList.size();
    }

    public static class CafeViewHolder extends RecyclerView.ViewHolder {
        TextView txtCafeName, txtCafeAddress, txtCafeDistance, txtCafeStatus;
        ImageView btnFavorite;

        public CafeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCafeName = itemView.findViewById(R.id.txtCafeName);
            txtCafeAddress = itemView.findViewById(R.id.txtCafeAddress);
            txtCafeDistance = itemView.findViewById(R.id.txtCafeDistance);
            txtCafeStatus = itemView.findViewById(R.id.txtCafeStatus);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
