package com.example.onlineexamapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder> {

    private final Context context;
    private final List<Author> authorList;
    private final FirebaseFirestore fStore;
    private final String currentUserId;

    public AuthorAdapter(Context context, List<Author> authorList) {
        this.context = context;
        this.authorList = authorList;
        this.fStore = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_author, parent, false);
        return new AuthorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorViewHolder holder, int position) {
        Author author = authorList.get(position);
        holder.tvName.setText(author.getFullName());
        holder.tvUsername.setText("@" + author.getUsername());

        if (author.getProfilePic() != null && !author.getProfilePic().isEmpty()) {
            try {
                byte[] imageBytes = android.util.Base64.decode(author.getProfilePic(), android.util.Base64.DEFAULT);
                Glide.with(context)
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .into(holder.ivAvatar);
            } catch (Exception e) {
                holder.ivAvatar.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        // --- Follow / Unfollow logic ---
        if (currentUserId != null) {
            checkFollowStatus(author.getUid(), holder.btnFollow);
        }

        holder.btnFollow.setOnClickListener(v -> {
            toggleFollow(author, holder.btnFollow);
        });

        // Click on item to open Author Profile
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AuthorProfileActivity.class);
            intent.putExtra("authorUid", author.getUid());
            context.startActivity(intent);
        });
    }

    private void checkFollowStatus(String authorUid, AppCompatButton btnFollow) {
        fStore.collection("Following").document(currentUserId)
                .collection("UserFollowing").document(authorUid)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        btnFollow.setText("Unfollow");
                        btnFollow.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow.setText("Follow");
                        btnFollow.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                });
    }

    private void toggleFollow(Author author, AppCompatButton btnFollow) {
        if (currentUserId == null) {
            Toast.makeText(context, "Please sign in to follow authors", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorUid = author.getUid();
        boolean isCurrentlyFollowing = btnFollow.getText().toString().equals("Unfollow");

        WriteBatch batch = fStore.batch();

        DocumentReference followingRef = fStore.collection("Following").document(currentUserId)
                .collection("UserFollowing").document(authorUid);
        DocumentReference followersRef = fStore.collection("Followers").document(authorUid)
                .collection("UserFollowers").document(currentUserId);
        
        DocumentReference currentUserRef = fStore.collection("Users").document(currentUserId);
        DocumentReference targetAuthorRef = fStore.collection("Users").document(authorUid);

        if (isCurrentlyFollowing) {
            // Unfollow
            batch.delete(followingRef);
            batch.delete(followersRef);
            batch.update(currentUserRef, "followingCount", FieldValue.increment(-1));
            batch.update(targetAuthorRef, "followersCount", FieldValue.increment(-1));

            batch.commit().addOnSuccessListener(aVoid -> {
                btnFollow.setText("Follow");
                btnFollow.setBackgroundResource(R.drawable.bg_btn_follow);
            });
        } else {
            // Follow
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("timestamp", FieldValue.serverTimestamp());

            batch.set(followingRef, data);
            batch.set(followersRef, data);
            batch.update(currentUserRef, "followingCount", FieldValue.increment(1));
            batch.update(targetAuthorRef, "followersCount", FieldValue.increment(1));

            batch.commit().addOnSuccessListener(aVoid -> {
                btnFollow.setText("Unfollow");
                btnFollow.setBackgroundResource(R.drawable.bg_btn_unfollow);
            });
        }
    }

    @Override
    public int getItemCount() {
        return authorList.size();
    }

    static class AuthorViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvUsername;
        AppCompatButton btnFollow;

        public AuthorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvName = itemView.findViewById(R.id.tvAuthorName);
            tvUsername = itemView.findViewById(R.id.tvAuthorUsername);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }
}
