package com.example.onlineexamapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminAuthorAdapter extends RecyclerView.Adapter<AdminAuthorAdapter.AuthorViewHolder> {

    private List<UserModel> authorList;
    private List<UserModel> fullList;
    private FirebaseFirestore fStore;

    public AdminAuthorAdapter(List<UserModel> authorList) {
        this.authorList = authorList;
        this.fullList = new ArrayList<>(authorList);
        this.fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new AuthorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorViewHolder holder, int position) {
        UserModel author = authorList.get(position);
        holder.tvUserName.setText(author.getFull_name());
        holder.tvUserEmail.setText(author.getEmail());

        // Status Badges
        holder.tvBlockedBadge.setVisibility(author.getIsBlocked() ? View.VISIBLE : View.GONE);
        holder.tvAuthorBadge.setVisibility(View.VISIBLE);
        holder.tvAuthorBadge.setText("OFFICIAL AUTHOR");
        holder.tvAuthorBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6C5CE7")));

        if (author.getProfile_pic() != null && !author.getProfile_pic().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(author.getProfile_pic())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(holder.ivUserAvatar);
        } else {
            holder.ivUserAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Actions
        holder.btnDelete.setImageResource(R.drawable.ic_close); // Reuse for demote
        holder.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Revoke Author Status")
                    .setMessage("Are you sure you want to remove author privileges from " + author.getFull_name() + "?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        fStore.collection("Users").document(author.getId()).update("isAuthor", false)
                                .addOnSuccessListener(aVoid -> {
                                    int currentPos = authorList.indexOf(author);
                                    if (currentPos != -1) {
                                        authorList.remove(currentPos);
                                        fullList.remove(author);
                                        notifyItemRemoved(currentPos);
                                        Toast.makeText(v.getContext(), "Author status revoked", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.btnSuspend.setOnClickListener(v -> {
            boolean isBlocking = !author.getIsBlocked();
            fStore.collection("Users").document(author.getId()).update("isBlocked", isBlocking)
                    .addOnSuccessListener(aVoid -> {
                        author.setIsBlocked(isBlocking);
                        notifyItemChanged(position);
                        Toast.makeText(v.getContext(), isBlocking ? "Author suspended" : "Author restored", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return authorList.size();
    }

    public void filter(String query) {
        authorList.clear();
        if (query.isEmpty()) {
            authorList.addAll(fullList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (UserModel author : fullList) {
                if (author.getFull_name().toLowerCase().contains(lowerCaseQuery) ||
                    author.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    authorList.add(author);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<UserModel> newList) {
        this.authorList = newList;
        this.fullList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class AuthorViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar, btnDelete, btnSuspend;
        TextView tvUserName, tvUserEmail, tvBlockedBadge, tvAuthorBadge;

        public AuthorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSuspend = itemView.findViewById(R.id.btnSuspend);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvBlockedBadge = itemView.findViewById(R.id.tvBlockedBadge);
            tvAuthorBadge = itemView.findViewById(R.id.tvUserStatus);
        }
    }
}
