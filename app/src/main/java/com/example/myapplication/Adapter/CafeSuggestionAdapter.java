package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.CafeModel;
import com.example.myapplication.R;

import java.util.List;

public class CafeSuggestionAdapter extends RecyclerView.Adapter<CafeSuggestionAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(CafeModel cafe);
    }

    private List<CafeModel> filteredSuggestions;
    private OnSuggestionClickListener listener;

    public CafeSuggestionAdapter(List<CafeModel> suggestions, OnSuggestionClickListener listener) {
        this.filteredSuggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        CafeModel cafe = filteredSuggestions.get(position);
        holder.txtCafeName.setText(cafe.getName());
        holder.txtCafeAddress.setText(cafe.getAddress());

        Glide.with(holder.itemView.getContext())
                .load(cafe.getImg())
                .placeholder(R.drawable.default_img_coffee) // Ảnh mặc định khi loading
                .error(R.drawable.default_img_coffee) // Ảnh khi lỗi
                .into(holder.imgCafe);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSuggestionClick(cafe);
        });
    }

    @Override
    public int getItemCount() {
        return filteredSuggestions.size();
    }

    public void updateSuggestions(List<CafeModel> newSuggestions) {
        this.filteredSuggestions.clear();
        this.filteredSuggestions.addAll(newSuggestions);
        notifyDataSetChanged();
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCafe;
        TextView txtCafeName, txtCafeAddress;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCafe = itemView.findViewById(R.id.imgCafe);
            txtCafeName = itemView.findViewById(R.id.txtCafeName);
            txtCafeAddress = itemView.findViewById(R.id.txtCafeAddress);
        }
    }
}
