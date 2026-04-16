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
    private final String currentUserId;
    private final OnNotifClickListener listener;

    public interface OnNotifClickListener {
        void onNotifClick(NotificationModel model);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notifList, String currentUserId, OnNotifClickListener listener) {
        this.context = context;
        this.notifList = notifList;
        this.currentUserId = currentUserId;
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
        
        // --- Instagram style message ---
        holder.tvMessage.setText(getSafeText(model.getMessage(), "New notification"));

        if (model.getTimestamp() != null) {
            String timeAgo = (String) android.text.format.DateUtils.getRelativeTimeSpanString(
                    model.getTimestamp().getSeconds() * 1000,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS,
                    android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE);
            holder.tvTime.setText(timeAgo);
        } else {
            holder.tvTime.setText("");
        }

        // Unread dot logic removed as requested

        if (model.getSenderImage() != null && !model.getSenderImage().isEmpty()) {
            try {
                byte[] bytes = android.util.Base64.decode(model.getSenderImage(), android.util.Base64.DEFAULT);
                Glide.with(context).load(bytes).placeholder(R.drawable.ic_user_placeholder).into(holder.ivSender);
            } catch (Exception e) {
                holder.ivSender.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            holder.ivSender.setImageResource(R.drawable.ic_user_placeholder);
        }

        // --- Follow Back Button logic ---
        if ("follow".equalsIgnoreCase(model.getType()) && model.getSenderId() != null) {
            holder.btnFollowBack.setVisibility(View.VISIBLE);
            checkFollowStatus(model.getSenderId(), holder.btnFollowBack);
            holder.btnFollowBack.setOnClickListener(v -> toggleFollow(model.getSenderId(), holder.btnFollowBack));
        } else {
            holder.btnFollowBack.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotifClick(model);
            
            // Open sender profile if it's a follow notification
            if (model.getSenderId() != null) {
                android.content.Intent intent = new android.content.Intent(context, AuthorProfileActivity.class);
                intent.putExtra("authorUid", model.getSenderId());
                context.startActivity(intent);
            }
        });
    }

    private void checkFollowStatus(String targetUid, androidx.appcompat.widget.AppCompatButton btn) {
        if (currentUserId == null) return;
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Following").document(currentUserId)
                .collection("UserFollowing").document(targetUid)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        btn.setText("Following");
                        btn.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btn.setText("Follow Back");
                        btn.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                });
    }

    private void toggleFollow(String targetUid, androidx.appcompat.widget.AppCompatButton btn) {
        if (currentUserId == null) return;
        boolean isFollowing = btn.getText().toString().equalsIgnoreCase("Following");
        btn.setEnabled(false);

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.firestore.WriteBatch batch = db.batch();

        com.google.firebase.firestore.DocumentReference followingRef = db.collection("Following").document(currentUserId)
                .collection("UserFollowing").document(targetUid);
        com.google.firebase.firestore.DocumentReference followersRef = db.collection("Followers").document(targetUid)
                .collection("UserFollowers").document(currentUserId);
        
        com.google.firebase.firestore.DocumentReference currentUserRef = db.collection("Users").document(currentUserId);
        com.google.firebase.firestore.DocumentReference targetUserRef = db.collection("Users").document(targetUid);

        if (isFollowing) {
            batch.delete(followingRef);
            batch.delete(followersRef);
            batch.update(currentUserRef, "followingCount", com.google.firebase.firestore.FieldValue.increment(-1));
            batch.update(targetUserRef, "followersCount", com.google.firebase.firestore.FieldValue.increment(-1));
        } else {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
            batch.set(followingRef, data);
            batch.set(followersRef, data);
            batch.update(currentUserRef, "followingCount", com.google.firebase.firestore.FieldValue.increment(1));
            batch.update(targetUserRef, "followersCount", com.google.firebase.firestore.FieldValue.increment(1));
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            btn.setEnabled(true);
            if (isFollowing) {
                btn.setText("Follow Back");
                btn.setBackgroundResource(R.drawable.bg_btn_follow);
            } else {
                btn.setText("Following");
                btn.setBackgroundResource(R.drawable.bg_btn_unfollow);
                // No need to send follow back notification for now unless asked
            }
        }).addOnFailureListener(e -> btn.setEnabled(true));
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
        ImageView ivSender;
        TextView tvMessage, tvTime;
        androidx.appcompat.widget.AppCompatButton btnFollowBack;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSender = itemView.findViewById(R.id.ivNotifSender);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            btnFollowBack = itemView.findViewById(R.id.btnFollowBack);
        }
    }
}
