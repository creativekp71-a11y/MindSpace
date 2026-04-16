package com.example.onlineexamapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.text.TextUtils;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final Context context;
    private final List<FriendModel> friendList;
    private final String currentUserId;
    private final FirebaseFirestore db;

    public FriendAdapter(Context context, List<FriendModel> friendList, String currentUserId) {
        this.context = context;
        this.friendList = friendList;
        this.currentUserId = currentUserId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendModel model = friendList.get(position);

        if (holder.tvName != null) {
            holder.tvName.setText(TextUtils.isEmpty(model.getName()) ? "User" : model.getName());
        }
        if (holder.tvEmail != null) {
            holder.tvEmail.setText(TextUtils.isEmpty(model.getEmail()) ? "No email" : model.getEmail());
        }

        String profileImage = model.getProfileImage();
        if (holder.imgProfile != null) {
            if (!TextUtils.isEmpty(profileImage)) {
            try {
                byte[] imageBytes = Base64.decode(profileImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                if (bitmap != null) {
                    holder.imgProfile.setImageBitmap(bitmap);
                } else {
                    holder.imgProfile.setImageResource(R.drawable.ic_user_placeholder);
                }
            } catch (Exception e) {
                holder.imgProfile.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            holder.imgProfile.setImageResource(R.drawable.ic_user_placeholder);
        }
        }

        if (holder.btnFollow != null) {
            setButtonState(holder.btnFollow, model.isFollowed());

            holder.btnFollow.setOnClickListener(v -> {
                holder.btnFollow.setEnabled(false);

                if (model.isFollowed()) {
                    unfollowUser(model, holder);
                } else {
                    followUser(model, holder);
                }
            });
        }
    }

    private void followUser(FriendModel model, FriendViewHolder holder) {
        Map<String, Object> map = new HashMap<>();
        map.put("followedAt", System.currentTimeMillis());

        db.collection("Users")
                .document(currentUserId)
                .collection("following")
                .document(model.getUserId())
                .set(map)
                .addOnSuccessListener(unused -> {
                    model.setFollowed(true);
                    setButtonState(holder.btnFollow, true);
                    holder.btnFollow.setEnabled(true);
                    Toast.makeText(context, "Followed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    holder.btnFollow.setEnabled(true);
                    Toast.makeText(context, "Follow failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void unfollowUser(FriendModel model, FriendViewHolder holder) {
        db.collection("Users")
                .document(currentUserId)
                .collection("following")
                .document(model.getUserId())
                .delete()
                .addOnSuccessListener(unused -> {
                    model.setFollowed(false);
                    setButtonState(holder.btnFollow, false);
                    holder.btnFollow.setEnabled(true);
                    Toast.makeText(context, "Unfollowed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    holder.btnFollow.setEnabled(true);
                    Toast.makeText(context, "Unfollow failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setButtonState(AppCompatButton button, boolean isFollowed) {
        if (isFollowed) {
            button.setText("Unfollow");
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
        } else {
            button.setText("Follow");
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6C5CE7")));
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgProfile;
        TextView tvName, tvEmail;
        AppCompatButton btnFollow;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }
}