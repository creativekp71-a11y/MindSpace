package com.example.onlineexamapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private TextView tvName, tvUsername, tvEmail, tvPoints, tvCoins, tvRank, tvMenuAddActivity;
    private TextView tvFollowersCount, tvFollowingCount;
    private View viewDividerAddActivity;
    private SwitchCompat switchBecomeAuthor;
    private ImageView ivProfilePic, ivCover;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        tvName = view.findViewById(R.id.tvProfileName);
        tvUsername = view.findViewById(R.id.tvProfileUsername);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvPoints = view.findViewById(R.id.tvProfilePoints);
        tvCoins = view.findViewById(R.id.tvProfileCoins);
        tvRank = view.findViewById(R.id.tvProfileRank);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        ivCover = view.findViewById(R.id.ivCover);
        tvFollowersCount = view.findViewById(R.id.tvProfileFollowersCount);
        tvFollowingCount = view.findViewById(R.id.tvProfileFollowingCount);
        tvMenuAddActivity = view.findViewById(R.id.tvMenuAddActivity);
        viewDividerAddActivity = view.findViewById(R.id.viewDividerAddActivity);

        setupClickListeners(view);
        loadUserData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        view.findViewById(R.id.tvMenuSettings).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SettingsActivity.class)));

        view.findViewById(R.id.tvMenuLogout).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), SignInActivity.class));
            requireActivity().finish();
        });

        view.findViewById(R.id.tvMenuFollowing).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FollowingListActivity.class)));

        // ✅ My Achievements click added
        view.findViewById(R.id.tvMenuAchievements).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AchievementsActivity.class)));

        // ✅ Invite Friends / Share click added
        view.findViewById(R.id.tvMenuShare).setOnClickListener(v -> {
            String shareText = "📱 MindSpace Quiz App\n\n"
                    + "Hey! Try my app made for a college project.\n"
                    + "Download the APK from here:\n"
                    + "https://drive.google.com/file/d/1pwyLyBBJw3rjpffOcvoWWUUOkM1-IheX/view?usp=sharing"
                    + "Install it and enjoy the quiz app 🎯";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "MindSpace Quiz App");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Invite via"));
        });

        switchBecomeAuthor = view.findViewById(R.id.switchBecomeAuthor);
        switchBecomeAuthor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateAuthorStatus(isChecked);
        });

        tvMenuAddActivity.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddDiscoveryActivity.class)));
    }

    private void updateAuthorStatus(boolean isAuthor) {
        String uid = mAuth.getUid();
        if (uid != null) {
            fStore.collection("Users").document(uid).update("isAuthor", isAuthor)
                    .addOnSuccessListener(aVoid -> {
                        // success
                    });
        }
    }

    private void loadUserData() {
        String uid = mAuth.getUid();
        if (uid != null) {
            fStore.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    tvName.setText(doc.getString("full_name"));
                    tvUsername.setText("@" + doc.getString("username"));
                    tvEmail.setText(doc.getString("email"));
                    tvPoints.setText(String.valueOf(doc.get("points") != null ? doc.get("points") : 0));
                    tvCoins.setText(String.valueOf(doc.get("coins") != null ? doc.get("coins") : 0));
                    tvRank.setText(doc.getString("rank") != null ? doc.getString("rank") : "--");

                    Boolean isAuthor = doc.getBoolean("isAuthor");
                    switchBecomeAuthor.setChecked(isAuthor != null && isAuthor);
                    tvMenuAddActivity.setVisibility(isAuthor != null && isAuthor ? View.VISIBLE : View.GONE);
                    viewDividerAddActivity.setVisibility(isAuthor != null && isAuthor ? View.VISIBLE : View.GONE);

                    Long followers = doc.getLong("followersCount");
                    Long following = doc.getLong("followingCount");
                    tvFollowersCount.setText(String.valueOf(followers != null ? followers : 0));
                    tvFollowingCount.setText(String.valueOf(following != null ? following : 0));

                    String profilePic = doc.getString("profile_pic");
                    String coverPic = doc.getString("cover_pic");

                    if (profilePic != null && !profilePic.isEmpty()) {
                        byte[] pBytes = Base64.decode(profilePic, Base64.DEFAULT);
                        Glide.with(this).load(pBytes).placeholder(R.drawable.ic_user_placeholder).error(R.drawable.ic_user_placeholder).into(ivProfilePic);
                    }
                    if (coverPic != null && !coverPic.isEmpty()) {
                        byte[] cBytes = Base64.decode(coverPic, Base64.DEFAULT);
                        Glide.with(this).load(cBytes).placeholder(R.drawable.cover_photo).error(R.drawable.cover_photo).into(ivCover);
                    }
                }
            });
        }
    }
}