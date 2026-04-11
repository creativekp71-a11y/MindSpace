package com.example.onlineexamapp;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private final Context context;
    private final List<NotificationModel> notifList;
    private final OnNotifClickListener listener;

    public interface OnNotifClickListener {
        void onNotifClick(NotificationModel model);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notifList, OnNotifClickListener listener) {
        this.context = context;
        this.notifList = notifList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationModel model = notifList.get(position);
        holder.tvTitle.setText(getSafeText(model.getTitle(), "New notification"));
        holder.tvMessage.setText(getSafeText(model.getMessage(), "You have a new update."));

        if (model.getTimestamp() != null) {
            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(model.getTimestamp().getSeconds() * 1000);
            holder.tvTime.setText(timeAgo);
        } else {
            holder.tvTime.setText("");
        }

        holder.unreadDot.setVisibility(model.isRead() ? View.GONE : View.VISIBLE);
        holder.ivType.setImageResource(resolveNotificationIcon(model.getType()));

        if (model.getSenderImage() != null && !model.getSenderImage().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(model.getSenderImage(), Base64.DEFAULT);
                Glide.with(context).load(bytes).placeholder(R.drawable.ic_user_placeholder).into(holder.ivSender);
            } catch (Exception e) {
                holder.ivSender.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            holder.ivSender.setImageResource(R.drawable.ic_user_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotifClick(model);
        });
    }

    @Override
    public int getItemCount() {
        return notifList.size();
    }

    private String getSafeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private int resolveNotificationIcon(String type) {
        if (type == null) {
            return R.drawable.ic_notification;
        }

        switch (type.toLowerCase()) {
            case "new_discovery":
            case "discovery":
                return R.drawable.ic_nav_discover;
            case "quiz":
            case "quiz_update":
                return R.drawable.ic_nav_quiz;
            case "leaderboard":
            case "rank":
                return R.drawable.ic_nav_rank;
            case "profile":
                return R.drawable.ic_nav_profile;
            default:
                return R.drawable.ic_notification;
        }
    }

    class NotifViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSender, ivType;
        TextView tvTitle, tvMessage, tvTime;
        View unreadDot;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSender = itemView.findViewById(R.id.ivNotifSender);
            ivType = itemView.findViewById(R.id.ivNotifType);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            unreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
