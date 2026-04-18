package com.example.onlineexamapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.HashSet;
import java.util.Set;

public class MainHomeActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_TAB = "extra_open_tab";
    public static final String TAB_NOTIFICATIONS = "NOTIFICATIONS";
    private static final String PREFS_NOTIFICATIONS = "notification_prefs";
    private static final String KEY_PERMISSION_REQUESTED = "permission_requested";

    private ImageView ivHome, ivDiscover, ivNavCreate, ivRank, ivProfile;
    private TextView tvHome, tvDiscover, tvNavCreate, tvRank, tvProfile;
    private final int ACTIVE_COLOR = 0xFF6C5CE7; // Brand Purple
    private final int INACTIVE_COLOR = 0xFF888888; // Gray
    private final Set<String> deliveredNotificationIds = new HashSet<>();
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private ListenerRegistration notificationsListener;
    private boolean hasLoadedInitialNotifications;
    private FirebaseFirestore fStore;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        configureStatusBar();
        AppNotificationHelper.ensureChannel(this);

        fStore = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                }
        );

        // Initialize UI components for bottom nav
        ivHome = findViewById(R.id.ivNavHome);
        ivDiscover = findViewById(R.id.ivNavDiscover);
        ivNavCreate = findViewById(R.id.ivNavCreate);
        ivRank = findViewById(R.id.ivNavLeaderboard);
        ivProfile = findViewById(R.id.ivNavProfile);

        tvHome = findViewById(R.id.tvNavHome);
        tvDiscover = findViewById(R.id.tvNavDiscover);
        tvNavCreate = findViewById(R.id.tvNavCreate);
        tvRank = findViewById(R.id.tvNavLeaderboard);
        tvProfile = findViewById(R.id.tvNavProfile);

        // Click listeners for tabs
        findViewById(R.id.navHome).setOnClickListener(v -> loadFragment(new HomeFragment(), "HOME"));
        findViewById(R.id.navDiscover).setOnClickListener(v -> loadFragment(new DiscoverFragment(), "DISCOVER"));
        findViewById(R.id.navLeaderboard).setOnClickListener(v -> loadFragment(new RankFragment(), "RANK"));
        findViewById(R.id.navProfile).setOnClickListener(v -> loadFragment(new ProfileFragment(), "PROFILE"));

        // Create Quiz Tab
        findViewById(R.id.navCreate).setOnClickListener(v -> {
            Intent intent = new Intent(MainHomeActivity.this, AddDiscoveryActivity.class);
            startActivity(intent);
        });

        handleIntent(getIntent(), savedInstanceState == null);

        askNotificationPermissionIfNeeded();
        setupBackHandler();
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fm = getSupportFragmentManager();
                Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);

                if (currentFragment != null && !"HOME".equals(currentFragment.getTag())) {
                    // If not on Home tab, go back to Home
                    loadHomeFragment();
                } else {
                    // If on Home tab, exit app
                    setEnabled(false);
                    MainHomeActivity.this.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startNotificationsListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopNotificationsListener();
    }

    private void configureStatusBar() {
        // Detect if currently in dark mode
        boolean isDarkMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        // Use dark background in dark mode, white in light mode
        int statusColor = isDarkMode ? 0xFF121212 : 0xFFFFFFFF;
        getWindow().setStatusBarColor(statusColor);

        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            // Light icons on dark background, dark icons on white background
            insetsController.setAppearanceLightStatusBars(!isDarkMode);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            if (!isDarkMode) {
                decorView.setSystemUiVisibility(
                        decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            } else {
                decorView.setSystemUiVisibility(
                        decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }
    }

    public void loadFragment(Fragment fragment, String tag) {
        // Highlight active tab
        updateNavUI(tag);

        // Swap Fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment, tag);
        ft.commit();
    }

    public void loadHomeFragment() {
        loadFragment(new HomeFragment(), "HOME");
    }

    public void openNotifications() {
        loadFragment(new NotificationsFragment(), TAB_NOTIFICATIONS);
    }

    private void askNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        SharedPreferences preferences = getSharedPreferences(PREFS_NOTIFICATIONS, MODE_PRIVATE);
        boolean alreadyRequested = preferences.getBoolean(KEY_PERMISSION_REQUESTED, false);

        if (!alreadyRequested) {
            preferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply();
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void startNotificationsListener() {
        if (currentUid == null || notificationsListener != null) {
            return;
        }

        hasLoadedInitialNotifications = false;
        deliveredNotificationIds.clear();

        notificationsListener = fStore.collection("Notifications")
                .document(currentUid)
                .collection("UserNotifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(25)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    if (!hasLoadedInitialNotifications) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : value) {
                            deliveredNotificationIds.add(document.getId());
                        }
                        hasLoadedInitialNotifications = true;
                        return;
                    }

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() != DocumentChange.Type.ADDED) {
                            continue;
                        }

                        String notificationId = change.getDocument().getId();
                        if (!deliveredNotificationIds.add(notificationId)) {
                            continue;
                        }

                        String title = change.getDocument().getString("title");
                        String message = change.getDocument().getString("message");
                        String type = change.getDocument().getString("type");
                        String chatId = change.getDocument().getString("chatId");
                        String senderId = change.getDocument().getString("senderId");
                        String senderName = change.getDocument().getString("senderName");

                        if ("message".equals(type) && chatId != null) {
                            AppNotificationHelper.showChatNotification(
                                    this,
                                    notificationId.hashCode(),
                                    title == null || title.trim().isEmpty() ? "New Message" : title,
                                    message == null ? "" : message,
                                    chatId,
                                    senderId,
                                    senderName
                            );
                        } else if (("follow".equals(type) || "unfollow".equals(type)) && senderId != null) {
                            AppNotificationHelper.showFollowNotification(
                                    this,
                                    notificationId.hashCode(),
                                    title == null || title.trim().isEmpty() ? "Social Update" : title,
                                    message == null ? "" : message,
                                    senderId
                            );
                        } else {
                            AppNotificationHelper.showNotification(
                                    this,
                                    notificationId.hashCode(),
                                    title == null || title.trim().isEmpty() ? "MindSpace" : title,
                                    message == null || message.trim().isEmpty()
                                            ? "You have a new notification."
                                            : message
                            );
                        }
                    }
                });
    }

    private void stopNotificationsListener() {
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
    }

    private void updateNavUI(String tag) {
        // Reset all to inactive
        ivHome.setColorFilter(INACTIVE_COLOR);
        ivDiscover.setColorFilter(INACTIVE_COLOR);
        ivNavCreate.setColorFilter(INACTIVE_COLOR);
        ivRank.setColorFilter(INACTIVE_COLOR);
        ivProfile.setColorFilter(INACTIVE_COLOR);

        tvHome.setTextColor(INACTIVE_COLOR);
        tvDiscover.setTextColor(INACTIVE_COLOR);
        tvNavCreate.setTextColor(INACTIVE_COLOR);
        tvRank.setTextColor(INACTIVE_COLOR);
        tvProfile.setTextColor(INACTIVE_COLOR);

        // Set active
        switch (tag) {
            case "HOME":
                ivHome.setColorFilter(ACTIVE_COLOR);
                tvHome.setTextColor(ACTIVE_COLOR);
                break;
            case "DISCOVER":
                ivDiscover.setColorFilter(ACTIVE_COLOR);
                tvDiscover.setTextColor(ACTIVE_COLOR);
                break;
            case "CREATE":
                ivNavCreate.setColorFilter(ACTIVE_COLOR);
                tvNavCreate.setTextColor(ACTIVE_COLOR);
                break;
            case "RANK":
                ivRank.setColorFilter(ACTIVE_COLOR);
                tvRank.setTextColor(ACTIVE_COLOR);
                break;
            case "PROFILE":
                ivProfile.setColorFilter(ACTIVE_COLOR);
                tvProfile.setTextColor(ACTIVE_COLOR);
                break;
    }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent, true);
    }

    private void handleIntent(Intent intent, boolean shouldForceLoad) {
        if (intent == null) return;
        String initialTab = intent.getStringExtra(EXTRA_OPEN_TAB);

        if (TAB_NOTIFICATIONS.equals(initialTab)) {
            openNotifications();
        } else if ("DISCOVER".equals(initialTab)) {
            loadFragment(new DiscoverFragment(), "DISCOVER");
        } else if ("RANK".equals(initialTab)) {
            loadFragment(new RankFragment(), "RANK");
        } else if ("PROFILE".equals(initialTab)) {
            loadFragment(new ProfileFragment(), "PROFILE");
        } else if ("HOME".equals(initialTab)) {
            loadFragment(new HomeFragment(), "HOME");
        } else if (shouldForceLoad) {
            loadFragment(new HomeFragment(), "HOME");
        }
    }
}
