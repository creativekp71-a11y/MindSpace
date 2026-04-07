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

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder> {

    private final Context context;
    private final List<Author> authorList;

    public AuthorAdapter(Context context, List<Author> authorList) {
        this.context = context;
        this.authorList = authorList;
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
        holder.btnFollow.setOnClickListener(v -> {
            if (holder.btnFollow.getText().toString().equals("Follow")) {
                holder.btnFollow.setText("Unfollow");
                holder.btnFollow.setBackgroundResource(R.drawable.bg_btn_unfollow);
            } else {
                holder.btnFollow.setText("Follow");
                holder.btnFollow.setBackgroundResource(R.drawable.bg_btn_follow);
            }
        });
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
