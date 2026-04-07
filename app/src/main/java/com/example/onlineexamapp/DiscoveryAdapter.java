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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.DiscoveryViewHolder> {

    private Context context;
    private List<DiscoveryActivityModel> discoveryList;
    private boolean isHorizontal;

    public DiscoveryAdapter(Context context, List<DiscoveryActivityModel> discoveryList, boolean isHorizontal) {
        this.context = context;
        this.discoveryList = discoveryList;
        this.isHorizontal = isHorizontal;
    }

    @NonNull
    @Override
    public DiscoveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discovery, parent, false);
        if (!isHorizontal) {
            // Adjust width for vertical list
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(lp);
        }
        return new DiscoveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveryViewHolder holder, int position) {
        DiscoveryActivityModel model = discoveryList.get(position);
        holder.tvTitle.setText(model.getTitle());
        holder.tvCategory.setText(model.getCategory());

        // Placeholders based on category
        setCategoryPlaceholder(holder.ivImage, model.getCategory());

        // Handle Click
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, QuizActivity.class);
            intent.putExtra("QUIZ_CATEGORY", model.getCategory());
            intent.putExtra("DISCOVERY_ID", model.getId()); // Use document ID for dynamic quiz
            context.startActivity(intent);
        });

        // Fetch author name
        fetchAuthorName(model.getAuthorId(), holder.tvAuthorName, holder.ivAuthorPic);
    }

    private void fetchAuthorName(String authorId, TextView tvName, ImageView ivPic) {
        if (authorId == null || authorId.isEmpty()) return;
        
        FirebaseFirestore.getInstance().collection("Users").document(authorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("full_name");
                        String pic = documentSnapshot.getString("profile_pic");
                        tvName.setText(name != null ? name : "Unknown");
                        if (pic != null && !pic.isEmpty()) {
                            try {
                                byte[] imageBytes = android.util.Base64.decode(pic, android.util.Base64.DEFAULT);
                                Glide.with(context).load(imageBytes).placeholder(R.drawable.ic_user_placeholder).into(ivPic);
                            } catch (Exception e) {
                                ivPic.setImageResource(R.drawable.ic_user_placeholder);
                            }
                        } else {
                            ivPic.setImageResource(R.drawable.ic_user_placeholder);
                        }
                    }
                });
    }

    private void setCategoryPlaceholder(ImageView iv, String category) {
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
        return discoveryList.size();
    }

    public static class DiscoveryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivAuthorPic;
        TextView tvTitle, tvCategory, tvAuthorName;

        public DiscoveryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivDiscoveryImage);
            ivAuthorPic = itemView.findViewById(R.id.ivDiscoveryAuthorPic);
            tvTitle = itemView.findViewById(R.id.tvDiscoveryTitle);
            tvCategory = itemView.findViewById(R.id.tvDiscoveryCategory);
            tvAuthorName = itemView.findViewById(R.id.tvDiscoveryAuthorName);
        }
    }
}
