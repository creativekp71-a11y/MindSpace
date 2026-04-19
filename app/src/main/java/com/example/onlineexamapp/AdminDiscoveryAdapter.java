package com.example.onlineexamapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDiscoveryAdapter extends RecyclerView.Adapter<AdminDiscoveryAdapter.QuizViewHolder> {

    private List<DiscoveryActivityModel> quizList;
    private List<DiscoveryActivityModel> fullList;
    private FirebaseFirestore fStore;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public AdminDiscoveryAdapter(List<DiscoveryActivityModel> quizList) {
        this.quizList = quizList;
        this.fullList = new ArrayList<>(quizList);
        this.fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_discovery, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        DiscoveryActivityModel quiz = quizList.get(position);
        holder.tvQuizTitle.setText(quiz.getTitle());
        holder.tvQuizCategory.setText(quiz.getCategory());

        String dateStr = quiz.getTimestamp() != null ? dateFormat.format(quiz.getTimestamp().toDate()) : "Recent";
        int qCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        holder.tvQuizStats.setText(qCount + " Questions • " + dateStr);

        // Load Base64 Image
        if (quiz.getCover_pic() != null && !quiz.getCover_pic().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(quiz.getCover_pic(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivQuizCover.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.ivQuizCover.setImageResource(R.drawable.mindspace_logo);
            }
        } else {
            holder.ivQuizCover.setImageResource(R.drawable.mindspace_logo);
        }

        // Actions
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddDiscoveryActivity.class);
            intent.putExtra("IS_EDIT_MODE", true);
            intent.putExtra("DISCOVERY_ID", quiz.getId());
            v.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Permanent Deletion")
                    .setMessage("Are you sure you want to permanently delete '" + quiz.getTitle() + "'? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        fStore.collection("DiscoveryActivities").document(quiz.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    int currentPos = quizList.indexOf(quiz);
                                    if (currentPos != -1) {
                                        quizList.remove(currentPos);
                                        fullList.remove(quiz);
                                        notifyItemRemoved(currentPos);
                                        Toast.makeText(v.getContext(), "Content removed permanently", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public void filter(String query) {
        quizList.clear();
        if (query.isEmpty()) {
            quizList.addAll(fullList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (DiscoveryActivityModel quiz : fullList) {
                if (quiz.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    quiz.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                    quizList.add(quiz);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<DiscoveryActivityModel> newList) {
        this.quizList = newList;
        this.fullList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        ImageView ivQuizCover, btnEdit, btnDelete;
        TextView tvQuizTitle, tvQuizCategory, tvQuizStats;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            ivQuizCover = itemView.findViewById(R.id.ivQuizCover);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvQuizTitle = itemView.findViewById(R.id.tvQuizTitle);
            tvQuizCategory = itemView.findViewById(R.id.tvQuizCategory);
            tvQuizStats = itemView.findViewById(R.id.tvQuizStats);
        }
    }
}
