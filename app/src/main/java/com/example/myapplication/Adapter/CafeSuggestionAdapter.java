package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class CafeSuggestionAdapter extends RecyclerView.Adapter<CafeSuggestionAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String name);
    }
    private List<String> filteredSuggestions;
    private OnSuggestionClickListener listener;

    public CafeSuggestionAdapter(List<String> suggestions, OnSuggestionClickListener listener) {
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
        String suggestion = filteredSuggestions.get(position);
        holder.textView.setText(suggestion);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSuggestionClick(suggestion);
        });
    }

    @Override
    public int getItemCount() {
        return filteredSuggestions.size();
    }

    public void updateSuggestions(List<String> newSuggestions) {
        this.filteredSuggestions.clear();
        this.filteredSuggestions.addAll(newSuggestions);

        notifyDataSetChanged();
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txtSuggestion);
        }
    }
}
