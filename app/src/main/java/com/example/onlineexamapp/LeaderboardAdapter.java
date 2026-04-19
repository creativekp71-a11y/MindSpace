package com.example.onlineexamapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userList;

    public LeaderboardAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);

        // Set Rank Number
        holder.tvRank.setText("#" + (position + 1));

        // Set Name & Username
        holder.tvName.setText(user.getFull_name());
        holder.tvUsername.setText("@" + user.getUsername());

        // Set Points
        long pts = user.getPoints() != null ? user.getPoints() : 0;
        holder.tvPoints.setText(String.valueOf(Math.max(0, pts)));

        // Load Profile Pic
        if (user.getProfile_pic() != null && !user.getProfile_pic().isEmpty()) {
            try {
                byte[] imageBytes = android.util.Base64.decode(user.getProfile_pic(), android.util.Base64.DEFAULT);
                Glide.with(context)
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .into(holder.ivProfile);
            } catch (Exception e) {
                holder.ivProfile.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Highlight Top 3
        if (position == 0) holder.tvRank.setTextColor(android.graphics.Color.parseColor("#FFD700")); // Gold
        else if (position == 1) holder.tvRank.setTextColor(android.graphics.Color.parseColor("#C0C0C0")); // Silver
        else if (position == 2) holder.tvRank.setTextColor(android.graphics.Color.parseColor("#CD7F32")); // Bronze
        else holder.tvRank.setTextColor(android.graphics.Color.parseColor("#2F3640"));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvUsername, tvPoints;
        ImageView ivProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRankNumber);
            tvName = itemView.findViewById(R.id.tvLeaderboardName);
            tvUsername = itemView.findViewById(R.id.tvLeaderboardUsername);
            tvPoints = itemView.findViewById(R.id.tvLeaderboardPoints);
            ivProfile = itemView.findViewById(R.id.ivLeaderboardProfile);
        }
    }
}
