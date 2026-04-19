package com.example.onlineexamapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.ReportViewHolder> {

    private final Context context;
    private final List<ReportModel> reportList;
    private final FirebaseFirestore fStore;

    public AdminReportAdapter(Context context, List<ReportModel> reportList) {
        this.context = context;
        this.reportList = reportList;
        this.fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportModel model = reportList.get(position);

        holder.tvTarget.setText(model.getTargetName());
        holder.tvReason.setText("Reason: " + model.getReason());
        holder.tvDetails.setText(model.getDetails());
        holder.tvReporter.setText("By: " + model.getReporterName());
        holder.tvTypeTag.setText(model.getTargetType().toUpperCase());

        if (model.getTimestamp() != null) {
            String timeAgo = (String) android.text.format.DateUtils.getRelativeTimeSpanString(
                    model.getTimestamp().getSeconds() * 1000,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS,
                    android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE);
            holder.tvTime.setText(timeAgo);
        }

        holder.btnDismiss.setOnClickListener(v -> dismissReport(model, position));
        holder.btnAct.setOnClickListener(v -> showModerationDialog(model, position));
    }

    private void dismissReport(ReportModel model, int position) {
        fStore.collection("Reports").document(model.getId())
                .update("status", "dismissed")
                .addOnSuccessListener(aVoid -> {
                    reportList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Report dismissed", Toast.LENGTH_SHORT).show();
                });
    }

    private void showModerationDialog(ReportModel model, int position) {
        String actionTitle = model.getTargetType().equalsIgnoreCase("content") ? "Moderate Content" : "Moderate User";
        String actionButton = model.getTargetType().equalsIgnoreCase("content") ? "Delete Content" : "Restrict User";

        new AlertDialog.Builder(context)
                .setTitle(actionTitle)
                .setMessage("Decide how to handle this " + model.getTargetType() + " based on the community report.")
                .setPositiveButton(actionButton, (dialog, which) -> {
                    if (model.getTargetType().equalsIgnoreCase("content")) {
                        deleteContent(model, position);
                    } else {
                        restrictUser(model, position);
                    }
                })
                .setNeutralButton("Dismiss Report", (dialog, which) -> dismissReport(model, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteContent(ReportModel model, int position) {
        fStore.collection("DiscoveryActivities").document(model.getTargetId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Also resolve the report
                    fStore.collection("Reports").document(model.getId()).update("status", "resolved");
                    reportList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Content deleted and report resolved", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void restrictUser(ReportModel model, int position) {
        fStore.collection("Users").document(model.getTargetId())
                .update("isContentRestricted", true)
                .addOnSuccessListener(aVoid -> {
                    fStore.collection("Reports").document(model.getId()).update("status", "resolved");
                    reportList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "User restricted and report resolved", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvTarget, tvReason, tvDetails, tvReporter, tvTime, tvTypeTag;
        AppCompatButton btnDismiss, btnAct;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTarget = itemView.findViewById(R.id.tvReportTarget);
            tvReason = itemView.findViewById(R.id.tvReportReason);
            tvDetails = itemView.findViewById(R.id.tvReportDetails);
            tvReporter = itemView.findViewById(R.id.tvReporterName);
            tvTime = itemView.findViewById(R.id.tvReportTime);
            tvTypeTag = itemView.findViewById(R.id.tvReportTypeTag);
            btnDismiss = itemView.findViewById(R.id.btnDismissReport);
            btnAct = itemView.findViewById(R.id.btnActOnReport);
        }
    }
}
