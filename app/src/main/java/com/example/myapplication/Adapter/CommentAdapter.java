package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.CommentModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<CommentModel> commentList;
    private final SessionManager sessionManager;

    public CommentAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
        this.sessionManager = new SessionManager(context);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivMenu;
        TextView tvUserName, tvContent, tvTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel c = commentList.get(position);

        holder.tvUserName.setText(c.userName);
        holder.tvContent.setText(c.content);

        Date date = new Date(c.timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        holder.tvTime.setText(sdf.format(date));

        Glide.with(context)
                .load(c.userAvatar)
                .placeholder(R.drawable.ic_user)
                .circleCrop()
                .into(holder.ivAvatar);

        if (c.userId == sessionManager.getUserId()) {
            holder.ivMenu.setVisibility(View.VISIBLE);
            holder.ivMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.ivMenu);
                popup.inflate(R.menu.menu_comment_options);
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete_comment) {
                        new AlertDialog.Builder(context)
                                .setTitle("Xóa bình luận")
                                .setMessage("Bạn có chắc muốn xóa bình luận này?")
                                .setPositiveButton("Xóa", (dialog, which) -> {
                                    FirebaseFirestore.getInstance()
                                            .collection("comment")
                                            .document(c.id)
                                            .delete()
                                            .addOnSuccessListener(unused -> {
                                                commentList.remove(holder.getAdapterPosition());
                                                notifyItemRemoved(holder.getAdapterPosition());
                                                Toast.makeText(context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        } else {
            holder.ivMenu.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}

