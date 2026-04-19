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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

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
            Toast.makeText(context, "Opening: " + model.getTitle(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, QuizActivity.class);
            intent.putExtra("QUIZ_CATEGORY", model.getCategory());
            intent.putExtra("DISCOVERY_ID", model.getId());
            context.startActivity(intent);
        });

        // Click author to view profile
        View.OnClickListener authorClickListener = v -> {
            Intent intent = new Intent(context, AuthorProfileActivity.class);
            intent.putExtra("authorUid", model.getAuthorId());
            context.startActivity(intent);
        };
        holder.authorName.setOnClickListener(authorClickListener);
        holder.authorImage.setOnClickListener(authorClickListener);

        // 🔹 Author Data (Firebase)
        fetchAuthor(model.getAuthorId(), holder.authorName, holder.authorImage);

        // 🔹 More Options (Edit/Delete for Author, Report for Others)
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null) {
            holder.ivMoreOptions.setVisibility(View.VISIBLE);
            holder.ivMoreOptions.setOnClickListener(v -> {
                if (currentUserId.equals(model.getAuthorId())) {
                    showPopupMenu(holder.ivMoreOptions, model, position);
                } else {
                    showReportPopupMenu(holder.ivMoreOptions, model);
                }
            });
        } else {
            holder.ivMoreOptions.setVisibility(View.GONE);
        }
    }

    private void showReportPopupMenu(View view, DiscoveryActivityModel model) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add("Report Content");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Report Content")) {
                showReportDialog(model);
            }
            return true;
        });
        popup.show();
    }

    private void showReportDialog(DiscoveryActivityModel model) {
        String[] reasons = {"Inappropriate Content", "Spam", "Harassment", "Copyright Violation", "Other"};
        new AlertDialog.Builder(context)
                .setTitle("Report this Quiz")
                .setItems(reasons, (dialog, which) -> {
                    submitReport(model, reasons[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitReport(DiscoveryActivityModel model, String reason) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        FirebaseFirestore.getInstance().collection("Users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String reporterName = userDoc.getString("full_name");
            ReportModel report = new ReportModel(
                    currentUserId,
                    reporterName != null ? reporterName : "Anonymous",
                    model.getId(),
                    model.getTitle(),
                    "content",
                    reason,
                    "Reported from Discovery Feed"
            );

            FirebaseFirestore.getInstance().collection("Reports")
                    .add(report)
                    .addOnSuccessListener(docRef -> Toast.makeText(context, "Report submitted. Thank you for keeping MindSpace safe!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to submit report: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void showPopupMenu(View view, DiscoveryActivityModel model, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit")) {
                Intent intent = new Intent(context, AddDiscoveryActivity.class);
                intent.putExtra("IS_EDIT_MODE", true);
                intent.putExtra("DISCOVERY_ID", model.getId());
                context.startActivity(intent);
            } else if (item.getTitle().equals("Delete")) {
                showDeleteConfirmation(model, position);
            }
            return true;
        });
        popup.show();
    }

    private void showDeleteConfirmation(DiscoveryActivityModel model, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Discovery")
                .setMessage("Are you sure you want to delete this discovery activity?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseFirestore.getInstance().collection("DiscoveryActivities")
                            .document(model.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                list.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, list.size());
                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    public void updateList(List<DiscoveryActivityModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    // 🔥 ViewHolder (IMPORTANT - match with XML IDs)
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image, authorImage, ivMoreOptions;
        TextView title, category, authorName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // 🌟 पुरानी IDs को नई IDs से बदल दिया है 🌟
            image = itemView.findViewById(R.id.ivDiscoverThumb);
            authorImage = itemView.findViewById(R.id.ivAuthorThumb);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);

            title = itemView.findViewById(R.id.tvDiscoverTitle);
            category = itemView.findViewById(R.id.tvDiscoverStats);
            authorName = itemView.findViewById(R.id.tvAuthorName);
        }
    }
}