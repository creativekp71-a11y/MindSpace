package com.example.onlineexamapp;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.ViewHolder> {

    private Context context;
    private List<DiscoveryActivityModel> list;

    public DiscoveryAdapter(Context context, List<DiscoveryActivityModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discovery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DiscoveryActivityModel model = list.get(position);

        // 🔹 Set Data
        holder.title.setText(model.getTitle());
        holder.category.setText(model.getCategory());

        // 🔹 Cover Image (Custom or Category fallback)
        String coverPic = model.getCover_pic();
        if (coverPic != null && !coverPic.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(coverPic, Base64.DEFAULT);
                Glide.with(context)
                        .load(bytes)
                        .placeholder(R.drawable.quiz_photo_1)
                        .error(R.drawable.quiz_photo_1)
                        .into(holder.image);
            } catch (Exception e) {
                setCategoryImage(holder.image, model.getCategory());
            }
        } else {
            setCategoryImage(holder.image, model.getCategory());
        }

        // 🔹 Click Open Quiz
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuizActivity.class);
            intent.putExtra("QUIZ_CATEGORY", model.getCategory());
            intent.putExtra("DISCOVERY_ID", model.getId());
            context.startActivity(intent);
        });

        // 🔹 Author Data (Firebase)
        fetchAuthor(model.getAuthorId(), holder.authorName, holder.authorImage);
    }

    // 🔥 Fetch Author Name + Image
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

    // 🔥 Category Image Set
    private void setCategoryImage(ImageView iv, String category) {

        if (category == null) category = "fun";

        category = category.toLowerCase();

        if (category.contains("science")) {
            iv.setImageResource(R.drawable.quiz_photo_6);
        } else if (category.contains("math")) {
            iv.setImageResource(R.drawable.quiz_photo_5);
        } else if (category.contains("brain")) {
            iv.setImageResource(R.drawable.quiz_photo_2);
        } else if (category.contains("fun")) {
            iv.setImageResource(R.drawable.quiz_photo_3);
        } else {
            iv.setImageResource(R.drawable.quiz_photo_1);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // 🔥 ViewHolder (IMPORTANT - match with XML IDs)
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image, authorImage;
        TextView title, category, authorName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // 🌟 पुरानी IDs को नई IDs से बदल दिया है 🌟
            image = itemView.findViewById(R.id.ivDiscoverThumb);
            authorImage = itemView.findViewById(R.id.ivAuthorThumb);

            title = itemView.findViewById(R.id.tvDiscoverTitle);
            category = itemView.findViewById(R.id.tvDiscoverStats);
            authorName = itemView.findViewById(R.id.tvAuthorName);
        }
    }
}