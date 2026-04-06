package com.example.onlineexamapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ContactSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search);

        // 1. Back button
        ImageView ivBack = findViewById(R.id.ivBackContact);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        LinearLayout contactContainer = findViewById(R.id.contactContainer);
        EditText etSearch = findViewById(R.id.etSearchContact);

        // ==========================================
        // 2. 👉 INVITE BUTTON KA JAADU 👈
        // ==========================================
        if (contactContainer != null) {
            // Saare contacts pe ek-ek karke jao
            for (int i = 0; i < contactContainer.getChildCount(); i++) {
                View childRow = contactContainer.getChildAt(i);

                if (childRow instanceof RelativeLayout) {
                    RelativeLayout row = (RelativeLayout) childRow;

                    // Us contact ke andar Invite button dhoondho
                    for (int j = 0; j < row.getChildCount(); j++) {
                        View rowElement = row.getChildAt(j);

                        if (rowElement instanceof AppCompatButton) {
                            AppCompatButton btnInvite = (AppCompatButton) rowElement;

                            // Button pe click hone ka logic
                            btnInvite.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (btnInvite.getText().toString().equalsIgnoreCase("Invite")) {
                                        // Click kiya -> Text "Invited" karo aur Color Grey (#A0A0A0) kar do
                                        btnInvite.setText("Invited");
                                        btnInvite.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A0A0A0")));
                                    } else {
                                        // Wapas click kiya -> Text "Invite" karo aur Color wapas Purple kar do
                                        btnInvite.setText("Invite");
                                        btnInvite.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6C5CE7")));
                                    }
                                }
                            });
                            break; // Button mil gaya, loop khatam karo
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. 👉 SEARCH FILTER KA JAADU 👈
        // ==========================================
        if (etSearch != null && contactContainer != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String searchText = s.toString().toLowerCase().trim();

                    for (int i = 0; i < contactContainer.getChildCount(); i++) {
                        View childRow = contactContainer.getChildAt(i);

                        if (childRow instanceof RelativeLayout) {
                            RelativeLayout row = (RelativeLayout) childRow;
                            boolean matchFound = false;

                            for (int j = 0; j < row.getChildCount(); j++) {
                                View rowElement = row.getChildAt(j);
                                if (rowElement instanceof LinearLayout) {
                                    LinearLayout textLayout = (LinearLayout) rowElement;
                                    TextView nameView = (TextView) textLayout.getChildAt(0);
                                    String contactName = nameView.getText().toString().toLowerCase();

                                    if (contactName.contains(searchText)) {
                                        matchFound = true;
                                    }
                                    break;
                                }
                            }

                            if (matchFound) {
                                childRow.setVisibility(View.VISIBLE);
                            } else {
                                childRow.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
}