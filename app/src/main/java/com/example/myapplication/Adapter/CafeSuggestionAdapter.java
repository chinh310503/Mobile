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

    private List<String> allSuggestions;
    private List<String> filteredSuggestions;
    private OnSuggestionClickListener listener;

    public CafeSuggestionAdapter(List<String> suggestions, OnSuggestionClickListener listener) {
        this.allSuggestions = suggestions;
        this.filteredSuggestions = new ArrayList<>(suggestions);
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

    public void filter(String query) {
        filteredSuggestions.clear();
        if (query.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        for (String s : allSuggestions) {
            if (s.toLowerCase().contains(query.toLowerCase())) {
                filteredSuggestions.add(s);
            }
        }
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
