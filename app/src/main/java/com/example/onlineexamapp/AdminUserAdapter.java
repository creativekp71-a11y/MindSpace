package com.example.onlineexamapp;

import android.content.Intent;
import android.util.Base64;
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

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<UserModel> userList;
    private List<UserModel> fullList;
    private FirebaseFirestore fStore;

    public AdminUserAdapter(List<UserModel> userList) {
        this.userList = userList;
        this.fullList = new ArrayList<>(userList);
        this.fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.tvUserName.setText(user.getFull_name());
        holder.tvUserEmail.setText(user.getEmail());

        // Handle Status Badges
        if (user.getIsBlocked()) {
            holder.tvBlockedBadge.setVisibility(View.VISIBLE);
            holder.btnSuspend.setImageResource(R.drawable.ic_eye_closed_slash);
            holder.btnSuspend.setColorFilter(android.graphics.Color.parseColor("#D63031"));
        } else {
            holder.tvBlockedBadge.setVisibility(View.GONE);
            holder.btnSuspend.setImageResource(R.drawable.ic_eye_closed_slash);
            holder.btnSuspend.setColorFilter(android.graphics.Color.parseColor("#00B894"));
        }

        // Handle User Status Badge
        if (user.getIsAuthor() != null && user.getIsAuthor()) {
            holder.tvUserStatus.setText("AUTHOR");
            holder.tvUserStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6C5CE7")));
        } else {
            holder.tvUserStatus.setText("MEMBER");
            holder.tvUserStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#444444")));
        }

        // Handle Profile Picture (Base64)
        if (user.getProfile_pic() != null && !user.getProfile_pic().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(user.getProfile_pic(), Base64.DEFAULT);
                Glide.with(holder.itemView.getContext())
                        .load(bytes)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(holder.ivUserAvatar);
            } catch (Exception e) {
                holder.ivUserAvatar.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            holder.ivUserAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Open Full Profile on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AuthorProfileActivity.class);
            intent.putExtra("authorUid", user.getId());
            v.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to permanently delete " + user.getFull_name() + "? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        fStore.collection("Users").document(user.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    int currentPos = userList.indexOf(user);
                                    if (currentPos != -1) {
                                        userList.remove(currentPos);
                                        fullList.remove(user);
                                        notifyItemRemoved(currentPos);
                                        Toast.makeText(v.getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                                    String email = (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) ? 
                                            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail() : "Anonymous";
                                    Toast.makeText(v.getContext(), "Delete Failed!\nUID: " + uid + "\nEmail: " + email + "\nError: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.btnSuspend.setOnClickListener(v -> {
            boolean isBlocking = !user.getIsBlocked();
            String title = isBlocking ? "Block User" : "Unblock User";
            String message = isBlocking ? "Do you want to block " + user.getFull_name() + "?" : "Do you want to unblock " + user.getFull_name() + "?";

            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(isBlocking ? "Block" : "Unblock", (dialog, which) -> {
                        fStore.collection("Users").document(user.getId()).update("isBlocked", isBlocking)
                                .addOnSuccessListener(aVoid -> {
                                    user.setIsBlocked(isBlocking);
                                    notifyItemChanged(position);
                                    Toast.makeText(v.getContext(), isBlocking ? "User blocked" : "User unblocked", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void filter(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(fullList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (UserModel user : fullList) {
                if (user.getFull_name().toLowerCase().contains(lowerCaseQuery) ||
                    user.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    userList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<UserModel> newList) {
        this.userList = newList;
        this.fullList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar, btnDelete, btnSuspend;
        TextView tvUserName, tvUserEmail, tvUserStatus, tvBlockedBadge;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSuspend = itemView.findViewById(R.id.btnSuspend);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            tvBlockedBadge = itemView.findViewById(R.id.tvBlockedBadge);
        }
    }
}
