
package com.example.onlineexamapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FirestoreDebug {
    public static void debugCollections() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Check DiscoveryActivities
        db.collection("DiscoveryActivities").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("DiscoveryActivities count: " + task.getResult().size());
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    System.out.println("Doc ID: " + doc.getId() + " Fields: " + doc.getData().keySet());
                    break; // Just one
                }
            } else {
                System.err.println("Error fetching DiscoveryActivities: " + task.getException());
            }
        });
    }
}
