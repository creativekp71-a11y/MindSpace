package com.example.onlineexamapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class QuestionPreviewAdapter extends RecyclerView.Adapter<QuestionPreviewAdapter.ViewHolder> {

    private List<Map<String, String>> questionList;
    private OnQuestionActionListener listener;

    public interface OnQuestionActionListener {
        void onEdit(int position, Map<String, String> question);
        void onDelete(int position);
    }

    public QuestionPreviewAdapter(List<Map<String, String>> questionList, OnQuestionActionListener listener) {
        this.questionList = questionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> question = questionList.get(position);
        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvText.setText(question.get("question"));

        holder.ivEdit.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onEdit(pos, questionList.get(pos));
            }
        });
        holder.ivDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDelete(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvText;
        ImageView ivEdit, ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvQuestionPreviewNumber);
            tvText = itemView.findViewById(R.id.tvQuestionPreviewText);
            ivEdit = itemView.findViewById(R.id.ivEditQuestion);
            ivDelete = itemView.findViewById(R.id.ivDeleteQuestion);
        }
    }
}
