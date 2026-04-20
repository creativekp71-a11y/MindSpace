package com.example.onlineexamapp;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // ==========================================
        // 🔙 Back Button
        // ==========================================
        ImageView btnBackSearch = findViewById(R.id.btnBackSearch);
        if (btnBackSearch != null) {
            btnBackSearch.setOnClickListener(v -> finish());
        }

        EditText etSearchInput = findViewById(R.id.etSearchInput);
        ImageView btnSearchAction = findViewById(R.id.btnSearchAction);
        TextView tvSearchResult = findViewById(R.id.tvSearchResult);

        // ==========================================
        // 🔍 Search Icon पर क्लिक
        // ==========================================
        if (btnSearchAction != null && etSearchInput != null) {
            btnSearchAction.setOnClickListener(v -> {
                String query = etSearchInput.getText().toString().trim();
                performSearch(query, tvSearchResult, etSearchInput);
            });
        }

        // ==========================================
        // ⌨️ कीबोर्ड वाले 'Search' बटन पर क्लिक
        // ==========================================
        if (etSearchInput != null) {
            etSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        String query = etSearchInput.getText().toString().trim();
                        performSearch(query, tvSearchResult, etSearchInput);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    // 🚀 असली सर्च का काम यहाँ होगा
    private void performSearch(String query, TextView tvSearchResult, EditText editText) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Please type something to search! 🧐", Toast.LENGTH_SHORT).show();
        } else {
            // कीबोर्ड को बंद करो
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            // अभी के लिए स्क्रीन पर टेक्स्ट बदल दो (बाद में यहाँ Firebase/API की लिस्ट आएगी)
            if (tvSearchResult != null) {
                // Stealth Admin: Ensure search results are filtered (logic will be added when results are live)
                tvSearchResult.setText("Searching for: '" + query + "'...\n(Results are screened for system accounts)");
            }
            Toast.makeText(this, "Searching: " + query, Toast.LENGTH_SHORT).show();
        }
    }
}