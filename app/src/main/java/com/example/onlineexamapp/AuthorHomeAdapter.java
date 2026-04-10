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

public class AuthorHomeAdapter extends RecyclerView.Adapter<AuthorHomeAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> authorList;

    public AuthorHomeAdapter(Context context, List<UserModel> authorList) {
        this.context = context;
        this.authorList = authorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_author_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel author = authorList.get(position);
        
        holder.tvName.setText(author.getFull_name());
        
        if (author.getProfile_pic() != null && !author.getProfile_pic().isEmpty()) {
            try {
                byte[] imageBytes = android.util.Base64.decode(author.getProfile_pic(), android.util.Base64.DEFAULT);
                Glide.with(context)
                        .load(imageBytes)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .into(holder.ivPic);
            } catch (Exception e) {
                holder.ivPic.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            holder.ivPic.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Click to view author profile
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, AuthorProfileActivity.class);
            intent.putExtra("authorUid", author.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return authorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPic;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPic = itemView.findViewById(R.id.ivAuthorHomePic);
            tvName = itemView.findViewById(R.id.tvAuthorHomeName);
        }
    }
}
