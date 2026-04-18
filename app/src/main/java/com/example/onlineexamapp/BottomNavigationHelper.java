package com.example.onlineexamapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BottomNavigationHelper {

    public static void setupBottomNavigation(Activity activity, int currentNavId) {
        View navHome = activity.findViewById(R.id.navHome);
        View navDiscover = activity.findViewById(R.id.navDiscover);
        View navCreate = activity.findViewById(R.id.navCreate);
        View navLeaderboard = activity.findViewById(R.id.navLeaderboard);
        View navProfile = activity.findViewById(R.id.navProfile);

        // Highlight current tab
        highlightTab(activity, currentNavId);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (currentNavId != R.id.navHome) {
                    Intent intent = new Intent(activity, MainHomeActivity.class);
                    intent.putExtra(MainHomeActivity.EXTRA_OPEN_TAB, "HOME");
                    navigateTo(activity, intent);
                }
            });
        }

        if (navDiscover != null) {
            navDiscover.setOnClickListener(v -> {
                if (currentNavId != R.id.navDiscover) {
                    Intent intent = new Intent(activity, MainHomeActivity.class);
                    intent.putExtra(MainHomeActivity.EXTRA_OPEN_TAB, "DISCOVER");
                    navigateTo(activity, intent);
                }
            });
        }

        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                if (currentNavId != R.id.navCreate) {
                    Intent intent = new Intent(activity, AddDiscoveryActivity.class);
                    navigateTo(activity, intent);
                }
            });
        }

        if (navLeaderboard != null) {
            navLeaderboard.setOnClickListener(v -> {
                if (currentNavId != R.id.navLeaderboard) {
                    Intent intent = new Intent(activity, MainHomeActivity.class);
                    intent.putExtra(MainHomeActivity.EXTRA_OPEN_TAB, "RANK");
                    navigateTo(activity, intent);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (currentNavId != R.id.navProfile) {
                    Intent intent = new Intent(activity, MainHomeActivity.class);
                    intent.putExtra(MainHomeActivity.EXTRA_OPEN_TAB, "PROFILE");
                    navigateTo(activity, intent);
                }
            });
        }
    }

    private static void highlightTab(Activity activity, int navId) {
        int activeColor = activity.getResources().getColor(R.color.purple_500);
        
        if (navId == R.id.navHome) {
            updateTabAppearance(activity, R.id.ivNavHome, R.id.tvNavHome, activeColor);
        } else if (navId == R.id.navDiscover) {
            updateTabAppearance(activity, R.id.ivNavDiscover, R.id.tvNavDiscover, activeColor);
        } else if (navId == R.id.navCreate) {
            updateTabAppearance(activity, R.id.ivNavCreate, R.id.tvNavCreate, activeColor);
        } else if (navId == R.id.navLeaderboard) {
            updateTabAppearance(activity, R.id.ivNavLeaderboard, R.id.tvNavLeaderboard, activeColor);
        } else if (navId == R.id.navProfile) {
            updateTabAppearance(activity, R.id.ivNavProfile, R.id.tvNavProfile, activeColor);
        }
    }

    private static void updateTabAppearance(Activity activity, int iconId, int textId, int color) {
        ImageView icon = activity.findViewById(iconId);
        TextView text = activity.findViewById(textId);
        if (icon != null) icon.setColorFilter(color);
        if (text != null) {
            text.setTextColor(color);
            text.setTypeface(null, Typeface.BOLD);
        }
    }

    private static void navigateTo(Activity activity, Class<?> targetClass) {
        navigateTo(activity, new Intent(activity, targetClass));
    }

    private static void navigateTo(Activity activity, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        if (!(activity instanceof MainHomeActivity)) {
            activity.finish();
        }
    }
}
