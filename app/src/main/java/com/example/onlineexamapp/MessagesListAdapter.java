package com.example.onlineexamapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ConversationViewHolder> {

    private final Context context;
    private final List<ConversationModel> conversationList;
    private final String currentUserId;
    private final FirebaseFirestore fStore;

    public MessagesListAdapter(Context context, List<ConversationModel> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
        this.fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ConversationModel model = conversationList.get(position);
        
        String otherUserId = "";
        for (String participant : model.getParticipants()) {
            if (!participant.equals(currentUserId)) {
                otherUserId = participant;
                break;
            }
        }

        // Load other user details
        loadOtherUserDetails(otherUserId, holder);

        holder.tvLastMessage.setText(model.getLastMessage());
        holder.tvTime.setText(formatTimestamp(model.getLastTimestamp()));

        Map<String, Integer> unreadMap = model.getUnreadCount();
        if (unreadMap != null && unreadMap.containsKey(currentUserId)) {
            Integer count = unreadMap.get(currentUserId);
            if (count != null && count > 0) {
                holder.viewUnreadDot.setVisibility(View.VISIBLE);
                holder.tvName.setTypeface(null, Typeface.BOLD);
                holder.tvLastMessage.setTypeface(null, Typeface.BOLD);
                holder.tvLastMessage.setTextColor(context.getResources().getColor(android.R.color.white));
            } else {
                holder.viewUnreadDot.setVisibility(View.GONE);
                holder.tvName.setTypeface(null, Typeface.NORMAL);
                holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
                holder.tvLastMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            holder.viewUnreadDot.setVisibility(View.GONE);
        }

        String finalOtherUserId = otherUserId;
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", model.getChatId());
            intent.putExtra("receiverId", finalOtherUserId);
            intent.putExtra("receiverName", holder.tvName.getText().toString());
            context.startActivity(intent);
        });
    }

    private void loadOtherUserDetails(String otherUserId, ConversationViewHolder holder) {
        fStore.collection("Users").document(otherUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String fullName = doc.getString("full_name");
                String profilePic = doc.getString("profile_pic");

                holder.tvName.setText(fullName != null ? fullName : "User");

                if (profilePic != null && !profilePic.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(profilePic, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.imgProfile.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        holder.imgProfile.setImageResource(R.drawable.ic_user_placeholder);
                    }
                } else {
                    holder.imgProfile.setImageResource(R.drawable.ic_user_placeholder);
                }
            }
        });
    }

    private String formatTimestamp(Object timestamp) {
        if (timestamp == null) return "";
        
        long timeMillis = 0;
        if (timestamp instanceof Timestamp) {
            timeMillis = ((Timestamp) timestamp).toDate().getTime();
        } else if (timestamp instanceof Long) {
            timeMillis = (Long) timestamp;
        } else if (timestamp instanceof Date) {
            timeMillis = ((Date) timestamp).getTime();
        }

        if (timeMillis == 0) return "";

        long diff = System.currentTimeMillis() - timeMillis;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) return "now";
        if (minutes < 60) return minutes + "m";
        if (hours < 24) return hours + "h";
        return days + "d";
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgProfile;
        TextView tvName, tvLastMessage, tvTime;
        View viewUnreadDot;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
