package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TopAuthorsActivity extends AppCompatActivity {

    private RecyclerView rvTopAuthors;
    private AuthorAdapter authorAdapter;
    private List<Author> authorList;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_authors);

        fStore = FirebaseFirestore.getInstance();
        rvTopAuthors = findViewById(R.id.rvTopAuthors);
        authorList = new ArrayList<>();
        authorAdapter = new AuthorAdapter(this, authorList);
        rvTopAuthors.setAdapter(authorAdapter);

        // Back button logic
        ImageView ivBackAuthors = findViewById(R.id.ivBackAuthors);
        if (ivBackAuthors != null) {
            ivBackAuthors.setOnClickListener(v -> finish());
        }

        fetchAuthors();
    }

    private void fetchAuthors() {
        fStore.collection("Users")
                .whereEqualTo("isAuthor", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    authorList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Author author = new Author();
                        author.setUid(document.getId());
                        author.setFullName(document.getString("full_name"));
                        author.setUsername(document.getString("username"));
                        author.setProfilePic(document.getString("profile_pic"));
                        author.setAuthor(true);
                        authorList.add(author);
                    }
                    authorAdapter.notifyDataSetChanged();
                    
                    if (authorList.isEmpty()) {
                        Toast.makeText(this, "No authors found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching authors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}