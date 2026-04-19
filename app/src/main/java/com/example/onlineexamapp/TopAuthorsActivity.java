package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
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
    private View llEmptyState; // Added empty state view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_authors);

        fStore = FirebaseFirestore.getInstance();
        rvTopAuthors = findViewById(R.id.rvTopAuthors);
        llEmptyState = findViewById(R.id.llEmptyState);
        authorList = new ArrayList<>();
        authorAdapter = new AuthorAdapter(this, authorList);
        rvTopAuthors.setAdapter(authorAdapter);

        // Back button logic
        ImageView ivBackAuthors = findViewById(R.id.ivBackAuthors);
        if (ivBackAuthors != null) {
            ivBackAuthors.setOnClickListener(v -> finish());
        }

        // Search Logic
        ImageView ivSearchAuthors = findViewById(R.id.ivSearchAuthors);
        CardView cvSearch = findViewById(R.id.cvSearchContainer);
        EditText etSearch = findViewById(R.id.etSearchAuthors);
        ImageView ivCloseSearch = findViewById(R.id.ivCloseSearch);

        if (ivSearchAuthors != null && cvSearch != null && etSearch != null) {
            ivSearchAuthors.setOnClickListener(v -> {
                cvSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            });

            ivCloseSearch.setOnClickListener(v -> {
                cvSearch.setVisibility(View.GONE);
                etSearch.setText("");
                authorAdapter.filter("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            });

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    authorAdapter.filter(s.toString());
                    updateEmptyState(authorAdapter.getItemCount() == 0);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
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
                        String email = document.getString("email");
                        // Stealth Admin: Hide this specific account from the top authors page
                        if (email != null && email.equalsIgnoreCase("admin@mindspace.com")) {
                            continue;
                        }

                        Author author = new Author();
                        author.setUid(document.getId());
                        author.setFullName(document.getString("full_name"));
                        author.setUsername(document.getString("username"));
                        author.setProfilePic(document.getString("profile_pic"));
                        author.setAuthor(true);
                        authorList.add(author);
                    }
                    authorAdapter.updateList(authorList);
                    updateEmptyState(authorList.isEmpty());
                })
                .addOnFailureListener(e -> {
                    updateEmptyState(true);
                    Toast.makeText(this, "Error fetching authors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (llEmptyState != null) {
            llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvTopAuthors.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }
}