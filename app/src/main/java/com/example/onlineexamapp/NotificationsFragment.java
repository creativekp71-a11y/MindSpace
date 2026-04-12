package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifList;
    private FirebaseFirestore fStore;
    private String currentUid;
    private LinearLayout llEmpty;
    private TextView tvMarkAllRead;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        fStore = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();

        rvNotifications = view.findViewById(R.id.rvNotifications);
        llEmpty = view.findViewById(R.id.llEmptyNotifications);
        tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);

        // 🔙 Back Button
        ImageView ivBack = view.findViewById(R.id.ivBackNotifications);
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DashboardActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        notifList = new ArrayList<>();

        adapter = new NotificationAdapter(getContext(), notifList, model -> {
            if (!model.isRead()) {
                markAsRead(model.getId());
            }
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setHasFixedSize(true);
        rvNotifications.setAdapter(adapter);
        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());

        fetchNotifications();

        return view;
    }

    private void fetchNotifications() {
        if (currentUid == null) return;

        fStore.collection("Notifications").document(currentUid)
                .collection("UserNotifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    notifList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        NotificationModel model = doc.toObject(NotificationModel.class);
                        model.setId(doc.getId());
                        notifList.add(model);
                    }

                    updateContentState();
                    updateMarkAllAction();
                    adapter.notifyDataSetChanged();
                });
    }

    private void updateContentState() {
        boolean hasNotifications = !notifList.isEmpty();
        llEmpty.setVisibility(hasNotifications ? View.GONE : View.VISIBLE);
        rvNotifications.setVisibility(hasNotifications ? View.VISIBLE : View.GONE);
    }

    private void updateMarkAllAction() {
        boolean hasUnread = false;
        for (NotificationModel notification : notifList) {
            if (!notification.isRead()) {
                hasUnread = true;
                break;
            }
        }
        tvMarkAllRead.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
    }

    private void markAsRead(String notifId) {
        if (currentUid == null || notifId == null) return;
        fStore.collection("Notifications").document(currentUid)
                .collection("UserNotifications").document(notifId)
                .update("read", true);
    }

    private void markAllAsRead() {
        if (currentUid == null) return;

        WriteBatch batch = fStore.batch();
        boolean hasUnread = false;
        for (NotificationModel notification : notifList) {
            if (!notification.isRead() && notification.getId() != null) {
                hasUnread = true;
                batch.update(
                        fStore.collection("Notifications")
                                .document(currentUid)
                                .collection("UserNotifications")
                                .document(notification.getId()),
                        "read",
                        true
                );
            }
        }

        if (hasUnread) {
            batch.commit();
        }
    }
}