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

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<UserModel> userList;
    private List<UserModel> fullList;

    public AdminUserAdapter(List<UserModel> userList) {
        this.userList = userList;
        this.fullList = new ArrayList<>(userList);
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

        if (user.getProfile_pic() != null && !user.getProfile_pic().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfile_pic())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(holder.ivUserAvatar);
        } else {
            holder.ivUserAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        holder.btnDelete.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Delete " + user.getFull_name(), Toast.LENGTH_SHORT).show();
        });

        holder.btnSuspend.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Suspend " + user.getFull_name(), Toast.LENGTH_SHORT).show();
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
        TextView tvUserName, tvUserEmail, tvUserStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSuspend = itemView.findViewById(R.id.btnSuspend);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
        }
    }
}
