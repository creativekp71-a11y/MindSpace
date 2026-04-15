package com.example.onlineexamapp;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("575953218451-b9jvn82uhpo0oos6r7kkjad9cnvclc5g.apps.googleusercontent.com")
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(activity, gso);
    }

    public static void handleGoogleSignInResult(
            Activity activity,
            Intent data,
            FirebaseAuth auth,
            FirebaseFirestore store,
            Map<String, Object> seedProfile,
            Callback callback
    ) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                firebaseAuthWithGoogle(account.getIdToken(), auth, store, seedProfile, callback);
            } else {
                if (callback != null) {
                    callback.onError("Google account or ID Token is null.");
                    callback.onComplete();
                }
            }
        } catch (ApiException e) {
            String errorMessage = "Google sign in failed (Code: " + e.getStatusCode() + "): ";
            switch (e.getStatusCode()) {
                case 10: errorMessage += "Developer error (likely SHA-1 mismatch or wrong package name)."; break;
                case 7: errorMessage += "Network error. Please check your connection."; break;
                case 12501: errorMessage += "Sign-in cancelled by user."; break;
                case 12500: errorMessage += "Sign-in failed. Please check your Firebase configuration."; break;
                default: errorMessage += e.getMessage(); break;
            }
            if (callback != null) {
                callback.onError(errorMessage);
                callback.onComplete();
            }
        }
    }

    private static void firebaseAuthWithGoogle(
            String idToken,
            FirebaseAuth auth,
            FirebaseFirestore store,
            Map<String, Object> seedProfile,
            Callback callback
    ) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                String uid = auth.getCurrentUser().getUid();
                store.collection("Users").document(uid).get().addOnCompleteListener(docTask -> {
                    if (docTask.isSuccessful() && !docTask.getResult().exists()) {
                        // Create new profile if it doesn't exist
                        Map<String, Object> user = new HashMap<>();
                        // First apply seed data from the form (dob, phone, country, age, etc.)
                        if (seedProfile != null) user.putAll(seedProfile);

                        // Google account data takes priority for identity fields
                        String googleName = auth.getCurrentUser().getDisplayName();
                        String googleEmail = auth.getCurrentUser().getEmail();
                        String googlePhoto = auth.getCurrentUser().getPhotoUrl() != null
                                ? auth.getCurrentUser().getPhotoUrl().toString() : "";

                        if (googleName != null && !googleName.isEmpty())
                            user.put("full_name", googleName);
                        if (googleEmail != null && !googleEmail.isEmpty())
                            user.put("email", googleEmail);
                        user.put("profile_pic", googlePhoto);

                        // Generate a default username from email (e.g. "john.doe@gmail.com" -> "john.doe")
                        if (!user.containsKey("username") || user.get("username") == null) {
                            String defaultUsername = googleEmail != null
                                    ? googleEmail.split("@")[0].replaceAll("[^a-zA-Z0-9._]", "_")
                                    : "user_" + uid.substring(0, 6);
                            user.put("username", defaultUsername);
                        }

                        // Default values for fields not yet set
                        user.putIfAbsent("dob", "");
                        user.putIfAbsent("phone", "");
                        user.putIfAbsent("country", "");
                        user.putIfAbsent("age", "");
                        user.putIfAbsent("bio", "");
                        user.putIfAbsent("cover_pic", "");
                        user.putIfAbsent("points", 0);
                        user.putIfAbsent("coins", 0);
                        user.putIfAbsent("rank", "--");
                        user.putIfAbsent("isAuthor", false);
                        user.putIfAbsent("followersCount", 0);
                        user.putIfAbsent("followingCount", 0);

                        store.collection("Users").document(uid).set(user).addOnCompleteListener(setTask -> {
                            if (callback != null) {
                                if (setTask.isSuccessful()) callback.onSuccess();
                                else callback.onError("Failed to create profile: " + setTask.getException().getMessage());
                                callback.onComplete();
                            }
                        });
                    } else {
                        if (callback != null) {
                            callback.onSuccess();
                            callback.onComplete();
                        }
                    }
                });
            } else {
                if (callback != null) {
                    callback.onError("Firebase auth failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    callback.onComplete();
                }
            }
        });
    }
}