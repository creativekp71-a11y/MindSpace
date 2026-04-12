package com.example.onlineexamapp;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public final class SocialAuthHelper {

    public interface Callback {
        void onSuccess();
        void onError(String message);
        void onComplete();
    }

    private SocialAuthHelper() {
    }

    // Google sign-in abhi disabled hai, isliye null return karenge
    public static Object createGoogleSignInClient(Activity activity) {
        return null;
    }

    // Google sign-in result abhi handle nahi karna, sirf safe callback dena
    public static void handleGoogleSignInResult(
            Activity activity,
            Intent data,
            FirebaseAuth auth,
            FirebaseFirestore store,
            Map<String, Object> seedProfile,
            Callback callback
    ) {
        if (callback != null) {
            callback.onError("Google sign-in is disabled in this build.");
            callback.onComplete();
        }
    }
}