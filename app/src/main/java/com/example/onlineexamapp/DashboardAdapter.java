package com.example.onlineexamapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Base64;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import android.content.Intent;
import android.widget.Toast;

// 1. क्लास का नाम और जेनेरिक टाइप एकदम सही है
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private Context context;
    private List<DiscoveryActivityModel> list;

    // 2. Constructor
    public DashboardAdapter(Context context, List<DiscoveryActivityModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 3. Using a specialized dashboard card (wide style)
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardAdapter.ViewHolder holder, int position) {
        DiscoveryActivityModel model = list.get(position);

        holder.title.setText(model.getTitle());
        holder.category.setText(model.getCategory());

        // 🔹 Cover Image Loading (Same logic as DiscoveryAdapter)
        String coverPic = model.getCover_pic();
        if (coverPic != null && !coverPic.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(coverPic, Base64.DEFAULT);
                Glide.with(context)
                        .load(bytes)
                        .placeholder(R.drawable.quiz_photo_1)
                        .into(holder.image);
            } catch (Exception e) {
                holder.image.setImageResource(R.drawable.quiz_photo_1);
            }
        } else {
            holder.image.setImageResource(R.drawable.quiz_photo_1);
        }

        // 🔹 Author Data Fetching
        fetchAuthor(model.getAuthorId(), holder.authorName, holder.authorImage);

        // 🔹 Click Navigation
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuizActivity.class);
            intent.putExtra("QUIZ_CATEGORY", model.getCategory());
            intent.putExtra("DISCOVERY_ID", model.getId());
            context.startActivity(intent);
        });
    }

    private void fetchAuthor(String authorId, TextView name, ImageView image) {
        if (authorId == null || authorId.isEmpty()) return;

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(authorId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("full_name");
                        String profilePic = doc.getString("profile_pic");

                        name.setText(fullName != null ? fullName : "Unknown");

                        if (profilePic != null && !profilePic.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(profilePic, Base64.DEFAULT);
                                Glide.with(context)
                                        .load(bytes)
                                        .placeholder(R.drawable.ic_user_placeholder)
                                        .into(image);
                            } catch (Exception e) {
                                image.setImageResource(R.drawable.ic_user_placeholder);
                            }
                        } else {
                            image.setImageResource(R.drawable.ic_user_placeholder);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // 6. एकदम फ्रेश ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, authorImage;
        TextView title, category, authorName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 7. सारी सही IDs लगा दी गई हैं
            image = itemView.findViewById(R.id.ivDiscoverThumb);
            authorImage = itemView.findViewById(R.id.ivAuthorThumb);
            title = itemView.findViewById(R.id.tvDiscoverTitle);
            category = itemView.findViewById(R.id.tvDiscoverStats);
            authorName = itemView.findViewById(R.id.tvAuthorName);
        }
    }
}