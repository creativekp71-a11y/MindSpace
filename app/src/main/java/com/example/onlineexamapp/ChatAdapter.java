package com.example.onlineexamapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private final List<ChatMessageModel> messageList;
    private final String currentUserId;

    public ChatAdapter(Context context, List<ChatMessageModel> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageModel model = messageList.get(position);
        boolean isLastMessage = (position == messageList.size() - 1);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(model, isLastMessage);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(model);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvStatus;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(ChatMessageModel model, boolean isLastMessage) {
            tvMessage.setText(model.getMessageText());
            
            // Only show "Seen" status for the absolute latest message sent by current user
            if (isLastMessage && model.isSeen()) {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("Seen");
            } else {
                tvStatus.setVisibility(View.GONE);
            }
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(ChatMessageModel model) {
            tvMessage.setText(model.getMessageText());
            // Timestamps are hidden for received messages as per cleaner Instagram style
        }
    }
}
