package com.example.onlineexamapp;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SocialAuthHelper {

    public interface Callback {
        void onSuccess();
        void onError(String message);
        void onComplete();
    }

    private SocialAuthHelper() {
    }

    public static GoogleSignInClient createGoogleSignInClient(Activity activity) {
        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(activity.getString(R.string.default_web_client_id));

        return GoogleSignIn.getClient(activity, builder.build());
    }

    public static void handleGoogleSignInResult(
            Activity activity,
            Intent data,
            FirebaseAuth auth,
            FirebaseFirestore store,
            Map<String, Object> seedProfile,
            Callback callback
    ) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) {
                callback.onError("Google sign-in did not return a valid account.");
                callback.onComplete();
                return;
            }

            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential).addOnCompleteListener(activity, task -> {
                if (task.isSuccessful() && auth.getCurrentUser() != null) {
                    upsertUserDocument(store, auth.getCurrentUser(), seedProfile, callback);
                } else {
                    callback.onError(task.getException() != null
                            ? task.getException().getMessage()
                            : "Google sign-in failed.");
                    callback.onComplete();
                }
            });
        } catch (ApiException e) {
            callback.onError("Google sign-in failed: " + e.getStatusCode());
            callback.onComplete();
        }
    }

    private static void upsertUserDocument(
            FirebaseFirestore store,
            FirebaseUser firebaseUser,
            Map<String, Object> seedProfile,
            Callback callback
    ) {
        Map<String, Object> user = new HashMap<>();
        String email = valueOrDefault(firebaseUser.getEmail(), getString(seedProfile, "email"));
        String fullName = valueOrDefault(firebaseUser.getDisplayName(), getString(seedProfile, "full_name"));
        String phone = valueOrDefault(firebaseUser.getPhoneNumber(), getString(seedProfile, "phone"));

        user.put("full_name", valueOrDefault(fullName, "MindSpace User"));
        user.put("username", buildUsername(seedProfile, email, firebaseUser.getUid()));
        user.put("email", valueOrDefault(email, ""));
        user.put("phone", valueOrDefault(phone, ""));
        user.put("dob", getString(seedProfile, "dob"));
        user.put("country", valueOrDefault(getString(seedProfile, "country"), ""));
        user.put("age", valueOrDefault(getString(seedProfile, "age"), ""));
        user.put("points", 0);
        user.put("coins", 0);
        user.put("rank", "--");
        user.put("profile_pic", "");
        user.put("cover_pic", "");
        user.put("bio", "");
        user.put("isAuthor", false);
        user.put("followersCount", 0);
        user.put("followingCount", 0);

        store.collection("Users")
                .document(firebaseUser.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    callback.onSuccess();
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to save profile.");
                    callback.onComplete();
                });
    }

    private static String buildUsername(Map<String, Object> seedProfile, String email, String uid) {
        String provided = getString(seedProfile, "username");
        if (!TextUtils.isEmpty(provided)) {
            return provided;
        }

        String base = !TextUtils.isEmpty(email) && email.contains("@")
                ? email.substring(0, email.indexOf('@'))
                : "user_" + uid.substring(0, Math.min(6, uid.length()));

        base = base.toLowerCase(Locale.US).replaceAll("[^a-z0-9._]", "");
        if (base.length() < 3) {
            base = "user_" + uid.substring(0, Math.min(6, uid.length()));
        }
        return base;
    }

    private static String getString(Map<String, Object> values, String key) {
        if (values == null) {
            return "";
        }
        Object value = values.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private static String valueOrDefault(String first, String second) {
        if (!TextUtils.isEmpty(first)) {
            return first.trim();
        }
        return second == null ? "" : second.trim();
    }
}
