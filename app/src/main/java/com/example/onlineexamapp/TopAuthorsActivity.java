package com.example.onlineexamapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class TopAuthorsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_authors);

        // बैक बटन का लॉजिक
        ImageView ivBackAuthors = findViewById(R.id.ivBackAuthors);
        if (ivBackAuthors != null) {
            ivBackAuthors.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // पेज बंद करके वापस Dashboard पर जाओ
                }
            });
        }
        // ==========================================
        // 👉 Author 1 (Bhuri) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow1 = findViewById(R.id.btnFollow1);
        if (btnFollow1 != null) {
            btnFollow1.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    // अगर बटन पर 'Follow' लिखा है, तो उसे 'Unfollow' कर दो और रंग ग्रे कर दो
                    if (btnFollow1.getText().toString().equals("Follow")) {
                        btnFollow1.setText("Unfollow");
                        btnFollow1.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    }
                    // नहीं तो वापस 'Follow' कर दो और रंग पर्पल कर दो
                    else {
                        btnFollow1.setText("Follow");
                        btnFollow1.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 2 (Bhadi) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow2 = findViewById(R.id.btnFollow2);
        if (btnFollow2 != null) {
            btnFollow2.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow2.getText().toString().equals("Follow")) {
                        btnFollow2.setText("Unfollow");
                        btnFollow2.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow2.setText("Follow");
                        btnFollow2.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 3 (Bhago) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow3 = findViewById(R.id.btnFollow3);
        if (btnFollow3 != null) {
            btnFollow3.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow3.getText().toString().equals("Follow")) {
                        btnFollow3.setText("Unfollow");
                        btnFollow3.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow3.setText("Follow");
                        btnFollow3.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 4 (Guddi) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow4 = findViewById(R.id.btnFollow4);
        if (btnFollow4 != null) {
            btnFollow4.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow4.getText().toString().equals("Follow")) {
                        btnFollow4.setText("Unfollow");
                        btnFollow4.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow4.setText("Follow");
                        btnFollow4.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 5 (Sondo) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow5 = findViewById(R.id.btnFollow5);
        if (btnFollow5 != null) {
            btnFollow5.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow5.getText().toString().equals("Follow")) {
                        btnFollow5.setText("Unfollow");
                        btnFollow5.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow5.setText("Follow");
                        btnFollow5.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 6 (Ragho) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow6 = findViewById(R.id.btnFollow6);
        if (btnFollow6 != null) {
            btnFollow6.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow6.getText().toString().equals("Follow")) {
                        btnFollow6.setText("Unfollow");
                        btnFollow6.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow6.setText("Follow");
                        btnFollow6.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 7 (Pingro) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow7 = findViewById(R.id.btnFollow7);
        if (btnFollow7 != null) {
            btnFollow7.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow7.getText().toString().equals("Follow")) {
                        btnFollow7.setText("Unfollow");
                        btnFollow7.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow7.setText("Follow");
                        btnFollow7.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 8 (Pappu) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow8 = findViewById(R.id.btnFollow8);
        if (btnFollow8 != null) {
            btnFollow8.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow8.getText().toString().equals("Follow")) {
                        btnFollow8.setText("Unfollow");
                        btnFollow8.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow8.setText("Follow");
                        btnFollow8.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 9 (Bhikho) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow9 = findViewById(R.id.btnFollow9);
        if (btnFollow9 != null) {
            btnFollow9.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow9.getText().toString().equals("Follow")) {
                        btnFollow9.setText("Unfollow");
                        btnFollow9.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow9.setText("Follow");
                        btnFollow9.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 10 (Sibali) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow10 = findViewById(R.id.btnFollow10);
        if (btnFollow10 != null) {
            btnFollow10.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow10.getText().toString().equals("Follow")) {
                        btnFollow10.setText("Unfollow");
                        btnFollow10.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow10.setText("Follow");
                        btnFollow10.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 11 (Dhamo) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow11 = findViewById(R.id.btnFollow11);
        if (btnFollow11 != null) {
            btnFollow11.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow11.getText().toString().equals("Follow")) {
                        btnFollow11.setText("Unfollow");
                        btnFollow11.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow11.setText("Follow");
                        btnFollow11.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 12 (Gani) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow12 = findViewById(R.id.btnFollow12);
        if (btnFollow12 != null) {
            btnFollow12.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow12.getText().toString().equals("Follow")) {
                        btnFollow12.setText("Unfollow");
                        btnFollow12.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow12.setText("Follow");
                        btnFollow12.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 13 (Jojo) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow13 = findViewById(R.id.btnFollow13);
        if (btnFollow13 != null) {
            btnFollow13.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow13.getText().toString().equals("Follow")) {
                        btnFollow13.setText("Unfollow");
                        btnFollow13.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow13.setText("Follow");
                        btnFollow13.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }

        // ==========================================
        // 👉 Author 6 (Ghudi) का Follow/Unfollow लॉजिक 👈
        // ==========================================
        androidx.appcompat.widget.AppCompatButton btnFollow14 = findViewById(R.id.btnFollow14);
        if (btnFollow14 != null) {
            btnFollow14.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (btnFollow14.getText().toString().equals("Follow")) {
                        btnFollow14.setText("Unfollow");
                        btnFollow14.setBackgroundResource(R.drawable.bg_btn_unfollow);
                    } else {
                        btnFollow14.setText("Follow");
                        btnFollow14.setBackgroundResource(R.drawable.bg_btn_follow);
                    }
                }
            });
        }
    }


}