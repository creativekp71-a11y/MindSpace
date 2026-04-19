package com.example.onlineexamapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {

    private List<QuizQuestion> questionList;
    private int currentQuestionIndex = 0;
    private String selectedOption = "";

    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private boolean isAnswered = false;

    private TextView tvQuestion, tvOption1, tvOption2, tvOption3, tvOption4;
    private AppCompatButton btnSubmitNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvOption1 = findViewById(R.id.tvOption1);
        tvOption2 = findViewById(R.id.tvOption2);
        tvOption3 = findViewById(R.id.tvOption3);
        tvOption4 = findViewById(R.id.tvOption4);
        btnSubmitNext = findViewById(R.id.btnSubmitNext);

        questionList = new ArrayList<>();

        String category = getIntent().getStringExtra("QUIZ_CATEGORY");
        String discoveryId = getIntent().getStringExtra("DISCOVERY_ID");
        if (category == null) category = "Productivity";

        if (discoveryId != null && !discoveryId.isEmpty()) {
            fetchDynamicQuestions(discoveryId);
        } else {
            loadStaticQuestions(category);
        }

        View.OnClickListener optionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnswered) return;
                if (questionList == null || questionList.isEmpty()) return;

                isAnswered = true;
                resetOptions();

                // Apply selected styling
                v.setBackgroundResource(R.drawable.bg_option_selected);
                
                // Dark Mode specific overrides
                if (ThemeHelper.isDarkMode(QuizActivity.this)) {
                    v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6C63FF")));
                    ((TextView) v).setTextColor(android.graphics.Color.WHITE);
                }

                String fullSelectedText = ((TextView) v).getText().toString();
                QuizQuestion currentQ = questionList.get(currentQuestionIndex);

                // Robust answer matching (strips prefixes like (A), A., etc.)
                String cleanSelected = fullSelectedText.replaceAll("^[\\(\\[]?[A-D][\\.\\)\\:\\]]\\s*", "").trim();
                String cleanCorrect = currentQ.getCorrectAnswer().replaceAll("^[\\(\\[]?[A-D][\\.\\)\\:\\]]\\s*", "").trim();

                if (cleanSelected.equalsIgnoreCase(cleanCorrect)) {
                    selectedOption = fullSelectedText; // retain for state if needed
                    correctAnswers++;
                    updatePoints(true);
                    
                    // Audio feedback logic
                    SharedPreferences settings = getSharedPreferences("MindSpaceSettings", MODE_PRIVATE);
                    boolean isQuizSoundEnabled = settings.getBoolean("quiz_sound", true);
                    if (isQuizSoundEnabled) {
                        try {
                            MediaPlayer mediaPlayer = MediaPlayer.create(QuizActivity.this, R.raw.correct_sound);
                            if (mediaPlayer != null) {
                                mediaPlayer.start();
                                mediaPlayer.setOnCompletionListener(mp -> {
                                    mp.release();
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Toast.makeText(QuizActivity.this, "Right Answer! 🎉", Toast.LENGTH_SHORT).show();
                } else {
                    wrongAnswers++;
                    updatePoints(false);
                    Toast.makeText(QuizActivity.this, "Wrong Answer! ❌", Toast.LENGTH_SHORT).show();
                    triggerVibration();
                }
            }
        };

        tvOption1.setOnClickListener(optionClickListener);
        tvOption2.setOnClickListener(optionClickListener);
        tvOption3.setOnClickListener(optionClickListener);
        tvOption4.setOnClickListener(optionClickListener);

        btnSubmitNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (questionList == null || questionList.isEmpty()) {
                    Toast.makeText(QuizActivity.this, "No questions found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isAnswered) {
                    Toast.makeText(QuizActivity.this, "Please select an option first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentQuestionIndex < questionList.size() - 1) {
                    currentQuestionIndex++;
                    isAnswered = false;
                    displayQuestion();
                } else {
                    showResultDialog();
                }
            }
        });

        android.widget.ImageView ivBackQuiz = findViewById(R.id.ivBackQuiz);
        if (ivBackQuiz != null) {
            ivBackQuiz.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    finish();
                }
            });
        }
    }

    private void fetchDynamicQuestions(String discoveryId) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("DiscoveryActivities")
                .document(discoveryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, String>> dynamicQs =
                                (List<Map<String, String>>) documentSnapshot.get("questions");

                        if (dynamicQs != null && !dynamicQs.isEmpty()) {
                            questionList.clear();

                            for (Map<String, String> qMap : dynamicQs) {
                                questionList.add(new QuizQuestion(
                                        qMap.get("question"),
                                        qMap.get("optionA"),
                                        qMap.get("optionB"),
                                        qMap.get("optionC"),
                                        qMap.get("optionD"),
                                        qMap.get("correctAnswer")
                                ));
                            }

                            currentQuestionIndex = 0;
                            isAnswered = false;
                            displayQuestion();
                            Toast.makeText(this, "Dynamic questions loaded!", Toast.LENGTH_SHORT).show();
                        } else {
                            loadStaticQuestions(getIntent().getStringExtra("QUIZ_CATEGORY"));
                        }
                    } else {
                        loadStaticQuestions(getIntent().getStringExtra("QUIZ_CATEGORY"));
                    }
                })
                .addOnFailureListener(e -> {
                    loadStaticQuestions(getIntent().getStringExtra("QUIZ_CATEGORY"));
                });
    }

    private void loadStaticQuestions(String category) {
        if (category == null) category = "Productivity";

        questionList.clear();

        if (category.equals("Productivity")) {
            loadProductivityQuestions();
        } else if (category.equals("Brilliant Minds")) {
            loadBrilliantMindsQuestions();
        } else if (category.equals("Having Fun")) {
            loadHavingFunQuestions();
        } else if (category.equals("General Knowledge")) {
            loadGKQuestions();
        } else if (category.equals("Mathematics")) {
            loadMathQuestions();
        } else if (category.equals("Science")) {
            loadScienceQuestions();
        } else if (category.equals("Geography")) {
            loadGeographyQuestions();
        } else if (category.equals("Quick Play")) {
            loadQuickPlayQuestions();
        } else {
            Toast.makeText(this, "Loading default questions...", Toast.LENGTH_SHORT).show();
            loadProductivityQuestions();
        }

        if (questionList.size() > 0) {
            currentQuestionIndex = 0;
            isAnswered = false;
            displayQuestion();
        }
    }

    private void displayQuestion() {
        if (questionList == null || questionList.isEmpty()) return;
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questionList.size()) return;

        resetOptions();

        QuizQuestion currentQ = questionList.get(currentQuestionIndex);

        // Handle Question Number (Ensures "1. ", "2. " etc.)
        String questionText = currentQ.getQuestion();
        String qNumberPrefix = (currentQuestionIndex + 1) + ". ";
        // Remove existing number/symbol if present (e.g. "1. ", "1) ")
        String cleanQuestion = questionText.replaceAll("^\\d+[.\\)]\\s*", "");
        if (tvQuestion != null) tvQuestion.setText(qNumberPrefix + cleanQuestion);

        // Handle Options A, B, C, D
        setOptionText(tvOption1, "A", currentQ.getOptionA());
        setOptionText(tvOption2, "B", currentQ.getOptionB());
        setOptionText(tvOption3, "C", currentQ.getOptionC());
        setOptionText(tvOption4, "D", currentQ.getOptionD());

        if (btnSubmitNext != null) {
            if (currentQuestionIndex == questionList.size() - 1) {
                btnSubmitNext.setText("Submit");
            } else {
                btnSubmitNext.setText("Submit and Next");
            }
        }
    }

    private void setOptionText(TextView textView, String letter, String originalText) {
        if (textView == null || originalText == null) return;
        // Remove existing "A. ", "A) ", "(A) ", etc if they were hardcoded
        String cleanText = originalText.replaceAll("^[\\(\\[]?[A-D][\\.\\)\\:\\]]\\s*", "");
        textView.setText("(" + letter + ") " + cleanText);
    }

    private void resetOptions() {
        // Get the standard text color from theme attributes
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        int defaultTextColor = typedValue.data;
        
        TextView[] options = {tvOption1, tvOption2, tvOption3, tvOption4};
        for (TextView tv : options) {
            if (tv != null) {
                tv.setBackgroundResource(R.drawable.bg_quiz_option);
                tv.setBackgroundTintList(null); // Clear selection tint
                tv.setTextColor(defaultTextColor);
            }
        }
        selectedOption = "";
    }

    private void showResultDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_result);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            );
        }

        dialog.setCancelable(false);

        android.widget.TextView tvTotalScore = dialog.findViewById(R.id.tvTotalScore);
        android.widget.TextView tvRightScore = dialog.findViewById(R.id.tvRightScore);
        android.widget.TextView tvWrongScore = dialog.findViewById(R.id.tvWrongScore);
        android.widget.TextView tvPointsEarned = dialog.findViewById(R.id.tvPointsEarned);
        android.widget.TextView tvPointsDeducted = dialog.findViewById(R.id.tvPointsDeducted);
        androidx.appcompat.widget.AppCompatButton btnTaskComplete = dialog.findViewById(R.id.btnTaskComplete);

        int earned = correctAnswers * 5;
        int deducted = wrongAnswers * 2;

        tvTotalScore.setText("Total Questions: " + questionList.size());
        tvRightScore.setText("Correct Answers: " + correctAnswers + " ✅");
        tvWrongScore.setText("Wrong Answers: " + wrongAnswers + " ❌");
        tvPointsEarned.setText("Points Earned: " + earned);
        tvPointsDeducted.setText("Points Deducted: " + deducted);

        btnTaskComplete.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                dialog.dismiss();

                android.content.Intent intent =
                        new android.content.Intent(QuizActivity.this, MainHomeActivity.class);
                intent.putExtra(MainHomeActivity.EXTRA_OPEN_TAB, "DISCOVER");
                intent.addFlags(
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    // ─── Vibration Helper ────────────────────────────────────────────────────
    private void triggerVibration() {
        SharedPreferences prefs = getSharedPreferences("MindSpaceSettings", MODE_PRIVATE);
        boolean vibrationEnabled = prefs.getBoolean("vibration_enabled", true);
        if (!vibrationEnabled) return;

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26+ — use VibrationEffect (two short pulses = wrong-answer feel)
            vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, 120, 80, 120},   // delay, on, off, on (ms)
                    new int[]{0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE},
                    -1  // no repeat
            ));
        } else {
            // Legacy fallback
            vibrator.vibrate(new long[]{0, 120, 80, 120}, -1);
        }
    }

    private void updatePoints(boolean isCorrect) {
        android.content.SharedPreferences prefs = getSharedPreferences("MindSpacePrefs", MODE_PRIVATE);
        int currentPoints = prefs.getInt("total_points", 0);

        int pointDelta = isCorrect ? 5 : -2;
        currentPoints = Math.max(0, currentPoints + pointDelta);

        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("total_points", currentPoints);
        editor.apply();

        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore fStore =
                    com.google.firebase.firestore.FirebaseFirestore.getInstance();
            com.google.firebase.firestore.DocumentReference userRef = fStore.collection("Users").document(uid);

            fStore.runTransaction(transaction -> {
                com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(userRef);
                long oldPoints = 0;
                if (snapshot.exists() && snapshot.getLong("points") != null) {
                    oldPoints = snapshot.getLong("points");
                }
                long newPoints = Math.max(0, oldPoints + (long) pointDelta);
                transaction.update(userRef, "points", newPoints);
                return null;
            }).addOnFailureListener(e ->
                    android.util.Log.e("FirebasePoints", "Transaction failed: " + e.getMessage()));
        }
    }

    private void loadProductivityQuestions() {
        questionList.add(new QuizQuestion(
                "1. 'पोमोडोरो तकनीक' (Pomodoro Technique) क्या है?",
                "A) खाना बनाने का तरीका",
                "B) 25 मिनट काम, 5 मिनट का ब्रेक",
                "C) लगातार 8 घंटे सोना",
                "D) एक साथ 4 काम करना",
                "B) 25 मिनट काम, 5 मिनट का ब्रेक"
        ));

        questionList.add(new QuizQuestion(
                "2. '80/20 नियम' (Pareto Principle) का क्या मतलब है?",
                "A) 80% काम 20% समय में करना",
                "B) 80% आराम, 20% काम",
                "C) आपके 80% परिणाम 20% प्रयासों से आते हैं",
                "D) परीक्षा में 80 अंक लाना",
                "C) आपके 80% परिणाम 20% प्रयासों से आते हैं"
        ));

        questionList.add(new QuizQuestion(
                "3. उत्पादकता (Productivity) में 'ईट दैट फ्रॉग' का क्या अर्थ है?",
                "A) सुबह अच्छा नाश्ता करना",
                "B) सबसे मुश्किल काम सबसे पहले करना",
                "C) काम से छुट्टी लेना",
                "D) मेंढक पकड़ना",
                "B) सबसे मुश्किल काम सबसे पहले करना"
        ));

        questionList.add(new QuizQuestion(
                "4. 'टू-मिनट रूल' (Two-Minute Rule) हमें क्या सिखाता है?",
                "A) हर 2 मिनट में पानी पिएं",
                "B) मैगी 2 मिनट में बनाएं",
                "C) जो काम 2 मिनट से कम का हो, उसे तुरंत कर लें",
                "D) दिन में सिर्फ 2 मिनट काम करें",
                "C) जो काम 2 मिनट से कम का हो, उसे तुरंत कर लें"
        ));

        questionList.add(new QuizQuestion(
                "5. मल्टीटास्किंग (एक साथ कई काम) करने का सबसे बड़ा नुकसान क्या है?",
                "A) ध्यान भटकता है और गलतियां होती हैं",
                "B) दिमाग तेज होता है",
                "C) समय बचता है",
                "D) बॉस खुश होता है",
                "A) ध्यान भटकता है और गलतियां होती हैं"
        ));

        questionList.add(new QuizQuestion(
                "6. 'टाइम ब्लॉकिंग' (Time Blocking) का क्या फायदा है?",
                "A) पड़ोसी का समय खराब करना",
                "B) हर काम के लिए एक निश्चित समय तय करना",
                "C) घड़ी को तोड़ देना",
                "D) सोशल मीडिया ब्लॉक करना",
                "B) हर काम के लिए एक निश्चित समय तय करना"
        ));

        questionList.add(new QuizQuestion(
                "7. 'इनबॉक्स जीरो' (Inbox Zero) का क्या मतलब होता है?",
                "A) जीरो ईमेल आना",
                "B) फोन की मेमोरी फुल होना",
                "C) अपने ईमेल इनबॉक्स को पढ़कर पूरा खाली रखना",
                "D) स्पैम फोल्डर डिलीट करना",
                "C) अपने ईमेल इनबॉक्स को पढ़कर पूरा खाली रखना"
        ));

        questionList.add(new QuizQuestion(
                "8. इनमें से कौन सा ऐप टास्क (Task) और काम मैनेज करने के काम आता है?",
                "A) ट्रेलो (Trello)",
                "B) नेटफ्लिक्स (Netflix)",
                "C) इंस्टाग्राम (Instagram)",
                "D) स्पॉटिफाई (Spotify)",
                "A) ट्रेलो (Trello)"
        ));

        questionList.add(new QuizQuestion(
                "9. 'टू-डू लिस्ट' (To-Do List) बनाने का मुख्य फायदा क्या है?",
                "A) समय बर्बाद करना",
                "B) डेस्क पर कागज भरना",
                "C) कामों को याद रखना और उन्हें प्लान करना",
                "D) चित्र बनाना",
                "C) कामों को याद रखना और उन्हें प्लान करना"
        ));

        questionList.add(new QuizQuestion(
                "10. 'पार्किंसंस लॉ' (Parkinson's Law) के हिसाब से काम कैसा होता है?",
                "A) काम हमेशा जल्दी खत्म होता है",
                "B) काम उतना ही खिंचता है जितना उसको समय दिया जाता है",
                "C) काम कभी खत्म नहीं होता",
                "D) काम दूसरे लोग करते हैं",
                "B) काम उतना ही खिंचता है जितना उसको समय दिया जाता है"
        ));

        Toast.makeText(this, "10 शुद्ध हिंदी सवाल लोड हो गए!", Toast.LENGTH_SHORT).show();
    }

    private void loadBrilliantMindsQuestions() {
        questionList.add(new QuizQuestion(
                "1. एक आदमी बारिश में बिना छाते के जा रहा था, फिर भी उसके सिर का एक भी बाल नहीं भीगा। कैसे?",
                "A) वह तेज़ दौड़ रहा था",
                "B) वह गंजा था",
                "C) बारिश बहुत धीमी थी",
                "D) उसने टोपी पहनी थी",
                "B) वह गंजा था"
        ));

        questionList.add(new QuizQuestion(
                "2. वह क्या है जो ऊपर तो जाता है, लेकिन कभी नीचे नहीं आता?",
                "A) पतंग",
                "B) हवाई जहाज",
                "C) उम्र (Age)",
                "D) पहाड़",
                "C) उम्र (Age)"
        ));

        questionList.add(new QuizQuestion(
                "3. साल के किस महीने में 28 दिन होते हैं?",
                "A) सिर्फ फरवरी में",
                "B) साल के हर महीने में",
                "C) सिर्फ लीप ईयर (Leap Year) में",
                "D) किसी में नहीं",
                "B) साल के हर महीने में"
        ));

        questionList.add(new QuizQuestion(
                "4. मैं जितनी ज्यादा बढ़ती हूँ, आप उतना ही कम देख पाते हैं। मैं क्या हूँ?",
                "A) रौशनी",
                "B) हवा",
                "C) अँधेरा",
                "D) पानी",
                "C) अँधेरा"
        ));

        questionList.add(new QuizQuestion(
                "5. राम के पिता के तीन बेटे हैं: लव, कुश और तीसरे का नाम क्या है?",
                "A) लक्ष्मण",
                "B) राम",
                "C) भरत",
                "D) शत्रुघ्न",
                "B) राम"
        ));

        questionList.add(new QuizQuestion(
                "6. अगर एक इलेक्ट्रिक ट्रेन उत्तर (North) की तरफ जा रही है, तो उसका धुआँ किस तरफ जाएगा?",
                "A) दक्षिण की तरफ",
                "B) पूर्व की तरफ",
                "C) पश्चिम की तरफ",
                "D) इलेक्ट्रिक ट्रेन धुआँ नहीं देती",
                "D) इलेक्ट्रिक ट्रेन धुआँ नहीं देती"
        ));

        questionList.add(new QuizQuestion(
                "7. वह क्या है जिसे आप एक बार खाने के बाद दोबारा नहीं खाना चाहते, लेकिन फिर भी खाते हैं?",
                "A) करेला",
                "B) कड़वी दवा",
                "C) धोखा",
                "D) कसम",
                "C) धोखा"
        ));

        questionList.add(new QuizQuestion(
                "8. ऐसा कौन सा शब्द है जिसे हमेशा 'गलत' ही पढ़ा जाता है?",
                "A) सही",
                "B) गलत",
                "C) जवाब",
                "D) सवाल",
                "B) गलत"
        ));

        questionList.add(new QuizQuestion(
                "9. आपके पास एक टोकरी में 5 सेब हैं, आपने उनमें से 3 ले लिए। अब आपके पास कितने सेब हैं?",
                "A) 5",
                "B) 2",
                "C) 3",
                "D) 0",
                "C) 3"
        ));

        questionList.add(new QuizQuestion(
                "10. ऐसी कौन सी चीज़ है जो टूटने के बाद ही काम आती है?",
                "A) शीशा",
                "B) अंडा",
                "C) ताला",
                "D) कुर्सी",
                "B) अंडा"
        ));

        Toast.makeText(this, "Brilliant Minds Quiz Loaded! 🧠", Toast.LENGTH_SHORT).show();
    }

    private void loadHavingFunQuestions() {
        questionList.add(new QuizQuestion(
                "1. ऐसा कौन सा महीना है जिसमें लोग सबसे कम सोते हैं?",
                "A) जनवरी",
                "B) फरवरी",
                "C) मार्च",
                "D) दिसंबर",
                "B) फरवरी"
        ));

        questionList.add(new QuizQuestion(
                "2. अगर आप एक लाल पत्थर को नीले समंदर में फेंकेंगे तो क्या होगा?",
                "A) वह लाल हो जाएगा",
                "B) वह गीला हो जाएगा और डूब जाएगा",
                "C) वह तैरने लगेगा",
                "D) वह नीला हो जाएगा",
                "B) वह गीला हो जाएगा और डूब जाएगा"
        ));

        questionList.add(new QuizQuestion(
                "3. किस सवाल का जवाब आप कभी 'हाँ' (Yes) में नहीं दे सकते?",
                "A) क्या आप थक गए हैं?",
                "B) क्या आप मुझे सुन सकते हैं?",
                "C) क्या आप सो रहे हैं?",
                "D) क्या आप भूखे हैं?",
                "C) क्या आप सो रहे हैं?"
        ));

        questionList.add(new QuizQuestion(
                "4. ऐसा कौन सा रूम है जिसमें ना कोई खिड़की है और ना कोई दरवाज़ा?",
                "A) बाथरूम",
                "B) बेडरूम",
                "C) मशरूम",
                "D) डार्क रूम",
                "C) मशरूम"
        ));

        questionList.add(new QuizQuestion(
                "5. अगर 8 लोग एक दीवार को बनाने में 10 घंटे लगाते हैं, तो 4 लोग उसी दीवार को बनाने में कितना समय लेंगे?",
                "A) 20 घंटे",
                "B) 5 घंटे",
                "C) बिल्कुल समय नहीं, दीवार बन चुकी है",
                "D) 10 घंटे",
                "C) बिल्कुल समय नहीं, दीवार बन चुकी है"
        ));

        questionList.add(new QuizQuestion(
                "6. वह क्या है जो आपके सोते ही नीचे गिर जाती है और उठते ही ऊपर उठ जाती है?",
                "A) आपकी साँस",
                "B) आँखों की पलकें",
                "C) दिल की धड़कन",
                "D) आपकी उम्र",
                "B) आँखों की पलकें"
        ));

        questionList.add(new QuizQuestion(
                "7. वह कौन सी चीज़ है जो हमेशा आपके सामने होती है लेकिन आप उसे देख नहीं सकते?",
                "A) आपका भविष्य (Future)",
                "B) आपकी नाक",
                "C) आपका साया",
                "D) हवा",
                "A) आपका भविष्य (Future)"
        ));

        questionList.add(new QuizQuestion(
                "8. एक गाय की पूंछ किस तरफ होती है?",
                "A) उत्तर की तरफ",
                "B) दक्षिण की तरफ",
                "C) नीचे की तरफ",
                "D) ऊपर की तरफ",
                "C) नीचे की तरफ"
        ));

        questionList.add(new QuizQuestion(
                "9. ऐसा कौन सा शब्द है जिसमें फल, फूल और मिठाई तीनों के नाम आते हैं?",
                "A) रसगुल्ला",
                "B) जलेबी",
                "C) गुलाब जामुन",
                "D) बर्फी",
                "C) गुलाब जामुन"
        ));

        questionList.add(new QuizQuestion(
                "10. वह क्या है जिसके पास मुँह नहीं है लेकिन बोलती बहुत है?",
                "A) तस्वीर",
                "B) गूँज (Echo)",
                "C) किताब",
                "D) पेड़",
                "B) गूँज (Echo)"
        ));

        Toast.makeText(this, "Having Fun Quiz Loaded! 😂", Toast.LENGTH_SHORT).show();
    }

    private void loadGKQuestions() {
        questionList.add(new QuizQuestion(
                "1. भारत की राजधानी क्या है?",
                "A) मुंबई", "B) नई दिल्ली", "C) कोलकाता", "D) चेन्नई",
                "B) नई दिल्ली"
        ));

        questionList.add(new QuizQuestion(
                "2. विश्व का सबसे बड़ा महासागर (Ocean) कौन सा है?",
                "A) अटलांटिक महासागर", "B) हिंद महासागर", "C) प्रशांत महासागर (Pacific)", "D) आर्कटिक महासागर",
                "C) प्रशांत महासागर (Pacific)"
        ));

        questionList.add(new QuizQuestion(
                "3. हमारे सौरमंडल (Solar System) में सूर्य के सबसे निकट कौन सा ग्रह है?",
                "A) शुक्र (Venus)", "B) मंगल (Mars)", "C) पृथ्वी (Earth)", "D) बुध (Mercury)",
                "D) बुध (Mercury)"
        ));

        questionList.add(new QuizQuestion(
                "4. भारत के पहले प्रधानमंत्री कौन थे?",
                "A) महात्मा गांधी", "B) जवाहरलाल नेहरू", "C) सरदार वल्लभभाई पटेल", "D) डॉ. बी.आर. अंबेडकर",
                "B) जवाहरलाल नेहरू"
        ));

        questionList.add(new QuizQuestion(
                "5. टेलीफोन का आविष्कार किसने किया था?",
                "A) थॉमस एडिसन", "B) अल्बर्ट आइंस्टीन", "C) अलेक्जेंडर ग्राहम बेल", "D) निकोला टेस्ला",
                "C) अलेक्जेंडर ग्राहम बेल"
        ));

        questionList.add(new QuizQuestion(
                "6. पृथ्वी पर सबसे ऊँचा पर्वत (Mountain) कौन सा है?",
                "A) K2", "B) कंचनजंगा", "C) माउंट एवरेस्ट", "D) माउंट किलिमंजारो",
                "C) माउंट एवरेस्ट"
        ));

        questionList.add(new QuizQuestion(
                "7. एक सामान्य मानव शरीर में कुल कितनी हड्डियाँ (Bones) होती हैं?",
                "A) 206", "B) 208", "C) 210", "D) 196",
                "A) 206"
        ));

        questionList.add(new QuizQuestion(
                "8. भारत का राष्ट्रीय पक्षी (National Bird) कौन सा है?",
                "A) तोता", "B) कबूतर", "C) मोर", "D) बाज",
                "C) मोर"
        ));

        questionList.add(new QuizQuestion(
                "9. 'गेटवे ऑफ इंडिया' (Gateway of India) कहाँ स्थित है?",
                "A) नई दिल्ली", "B) मुंबई", "C) आगरा", "D) जयपुर",
                "B) मुंबई"
        ));

        questionList.add(new QuizQuestion(
                "10. हमारा राष्ट्रगान 'जन गण मन' किसने लिखा था?",
                "A) बंकिम चंद्र चटर्जी", "B) रबीन्द्रनाथ टैगोर", "C) स्वामी विवेकानंद", "D) मुंशी प्रेमचंद",
                "B) रबीन्द्रनाथ टैगोर"
        ));

        Toast.makeText(this, "15 GK Questions Loaded! 🌍", Toast.LENGTH_SHORT).show();
    }

    private void loadMathQuestions() {
        questionList.add(new QuizQuestion(
                "1. 25 + 45 का सही उत्तर क्या है?",
                "A) 60", "B) 70", "C) 80", "D) 65",
                "B) 70"
        ));

        questionList.add(new QuizQuestion(
                "2. 12 × 8 कितना होता है?",
                "A) 84", "B) 92", "C) 96", "D) 104",
                "C) 96"
        ));

        questionList.add(new QuizQuestion(
                "3. 144 का वर्गमूल (Square Root) क्या है?",
                "A) 10", "B) 12", "C) 14", "D) 16",
                "B) 12"
        ));

        questionList.add(new QuizQuestion(
                "4. BODMAS के नियम से हल करें: 8 + 2 × 5 = ?",
                "A) 50", "B) 15", "C) 18", "D) 10",
                "C) 18"
        ));

        questionList.add(new QuizQuestion(
                "5. एक त्रिभुज (Triangle) के तीनों आंतरिक कोणों (Angles) का योग कितना होता है?",
                "A) 90°", "B) 180°", "C) 360°", "D) 270°",
                "B) 180°"
        ));

        questionList.add(new QuizQuestion(
                "6. 5 का घन (Cube) यानी 5³ कितना होता है?",
                "A) 25", "B) 125", "C) 150", "D) 75",
                "B) 125"
        ));

        questionList.add(new QuizQuestion(
                "7. 200 का 15% कितना होगा?",
                "A) 15", "B) 20", "C) 30", "D) 45",
                "C) 30"
        ));

        questionList.add(new QuizQuestion(
                "8. अगर x + 5 = 12 है, तो x का मान क्या होगा?",
                "A) 7", "B) 8", "C) 5", "D) 17",
                "A) 7"
        ));

        questionList.add(new QuizQuestion(
                "9. एक वर्ग (Square) की भुजा 5 cm है। उसका परिमाप (Perimeter) क्या होगा?",
                "A) 10 cm", "B) 15 cm", "C) 20 cm", "D) 25 cm",
                "C) 20 cm"
        ));

        questionList.add(new QuizQuestion(
                "10. 100 ÷ 4 + 5 को हल करें:",
                "A) 10", "B) 20", "C) 30", "D) 25",
                "C) 30"
        ));

        Toast.makeText(this, "Math Quiz Loaded! 🧮", Toast.LENGTH_SHORT).show();
    }

    private void loadScienceQuestions() {
        questionList.add(new QuizQuestion(
                "1. पानी (Water) का रासायनिक सूत्र (Chemical Formula) क्या है?",
                "A) CO2", "B) O2", "C) H2O", "D) NaCl",
                "C) H2O"
        ));

        questionList.add(new QuizQuestion(
                "2. इंसान साँस लेते समय कौन सी गैस अंदर खींचता है?",
                "A) कार्बन डाइऑक्साइड", "B) ऑक्सीजन", "C) नाइट्रोजन", "D) हीलियम",
                "B) ऑक्सीजन"
        ));

        questionList.add(new QuizQuestion(
                "3. पृथ्वी (Earth) के सबसे नज़दीक कौन सा तारा (Star) है?",
                "A) ध्रुव तारा", "B) सूर्य (Sun)", "C) सीरियस (Sirius)", "D) अल्फा सेंटॉरी",
                "B) सूर्य (Sun)"
        ));

        questionList.add(new QuizQuestion(
                "4. वह कौन सा बल (Force) है जो चीज़ों को ज़मीन की तरफ खींचता है?",
                "A) चुंबकीय बल (Magnetic)", "B) घर्षण (Friction)", "C) गुरुत्वाकर्षण (Gravity)", "D) विद्युत बल",
                "C) गुरुत्वाकर्षण (Gravity)"
        ));

        questionList.add(new QuizQuestion(
                "5. सामान्य तापमान पर पानी कितने डिग्री सेल्सियस पर उबलने लगता है?",
                "A) 50°C", "B) 100°C", "C) 150°C", "D) 200°C",
                "B) 100°C"
        ));

        questionList.add(new QuizQuestion(
                "6. हमारे सौरमंडल का 'लाल ग्रह' (Red Planet) किसे कहा जाता है?",
                "A) शुक्र (Venus)", "B) बृहस्पति (Jupiter)", "C) शनि (Saturn)", "D) मंगल (Mars)",
                "D) मंगल (Mars)"
        ));

        questionList.add(new QuizQuestion(
                "7. कोशिका का 'पावरहाउस' (Powerhouse of the Cell) किसे कहा जाता है?",
                "A) न्यूक्लियस", "B) माइटोकॉन्ड्रिया", "C) राइबोसोम", "D) डीएनए (DNA)",
                "B) माइटोकॉन्ड्रिया"
        ));

        questionList.add(new QuizQuestion(
                "8. बर्फ (Ice) पानी की कौन सी अवस्था (State of Matter) है?",
                "A) ठोस (Solid)", "B) तरल (Liquid)", "C) गैस (Gas)", "D) प्लाज़्मा (Plasma)",
                "A) ठोस (Solid)"
        ));

        questionList.add(new QuizQuestion(
                "9. पेड़-पौधे अपना खाना बनाने (Photosynthesis) के लिए कौन सी गैस सोखते हैं?",
                "A) ऑक्सीजन", "B) कार्बन डाइऑक्साइड", "C) नाइट्रोजन", "D) हाइड्रोजन",
                "B) कार्बन डाइऑक्साइड"
        ));

        questionList.add(new QuizQuestion(
                "10. ऐसा कौन सा धातु (Metal) है जो कमरे के तापमान पर तरल (Liquid) अवस्था में रहता है?",
                "A) सोना (Gold)", "B) लोहा (Iron)", "C) एल्युमिनियम", "D) पारा (Mercury)",
                "D) पारा (Mercury)"
        ));

        Toast.makeText(this, "Science Quiz Loaded! 🔬", Toast.LENGTH_SHORT).show();
    }

    private void loadGeographyQuestions() {
        questionList.add(new QuizQuestion(
                "1. क्षेत्रफल के हिसाब से दुनिया का सबसे बड़ा देश कौन सा है?",
                "A) चीन", "B) भारत", "C) अमेरिका", "D) रूस (Russia)",
                "D) रूस (Russia)"
        ));

        questionList.add(new QuizQuestion(
                "2. दुनिया की सबसे लंबी नदी (Longest River) कौन सी है?",
                "A) अमेज़न नदी", "B) नील नदी (Nile)", "C) गंगा नदी", "D) मिसिसिपी नदी",
                "B) नील नदी (Nile)"
        ));

        questionList.add(new QuizQuestion(
                "3. दुनिया का सबसे ऊँचा पर्वत 'माउंट एवरेस्ट' किस देश में स्थित है?",
                "A) भारत", "B) चीन", "C) नेपाल", "D) भूटान",
                "C) नेपाल"
        ));

        questionList.add(new QuizQuestion(
                "4. दुनिया का सबसे छोटा देश कौन सा है?",
                "A) मोनाको", "B) मालदीव", "C) वेटिकन सिटी", "D) सिंगापुर",
                "C) वेटिकन सिटी"
        ));

        questionList.add(new QuizQuestion(
                "5. 'सहारा रेगिस्तान' (Sahara Desert) किस महाद्वीप में स्थित है?",
                "A) एशिया", "B) अफ्रीका", "C) ऑस्ट्रेलिया", "D) दक्षिण अमेरिका",
                "B) अफ्रीका"
        ));

        questionList.add(new QuizQuestion(
                "6. भारत के किस राज्य को 'उगते सूरज की भूमि' (Land of Rising Sun) कहा जाता है?",
                "A) असम", "B) गुजरात", "C) अरुणाचल प्रदेश", "D) सिक्किम",
                "C) अरुणाचल प्रदेश"
        ));

        questionList.add(new QuizQuestion(
                "7. दुनिया का सबसे बड़ा महाद्वीप (Continent) कौन सा है?",
                "A) यूरोप", "B) उत्तरी अमेरिका", "C) अफ्रीका", "D) एशिया",
                "D) एशिया"
        ));

        questionList.add(new QuizQuestion(
                "8. 'स्टैच्यू ऑफ लिबर्टी' (Statue of Liberty) किस शहर में स्थित है?",
                "A) पेरिस", "B) वाशिंगटन डीसी", "C) न्यूयॉर्क", "D) लंदन",
                "C) न्यूयॉर्क"
        ));

        questionList.add(new QuizQuestion(
                "9. भारत की सबसे लंबी नदी कौन सी है?",
                "A) यमुना", "B) गोदावरी", "C) ब्रह्मपुत्र", "D) गंगा",
                "D) गंगा"
        ));

        questionList.add(new QuizQuestion(
                "10. जापान की राजधानी क्या है?",
                "A) बीजिंग", "B) सियोल", "C) टोक्यो", "D) क्योटो",
                "C) टोक्यो"
        ));

        Toast.makeText(this, "Geography Quiz Loaded! 🗺️", Toast.LENGTH_SHORT).show();
    }

    private void loadQuickPlayQuestions() {
        questionList.add(new QuizQuestion(
                "1. (Science) पानी का रासायनिक सूत्र क्या है?",
                "A) O2", "B) CO2", "C) H2O", "D) NaCl",
                "C) H2O"
        ));

        questionList.add(new QuizQuestion(
                "2. (Math) 15 × 6 कितना होता है?",
                "A) 80", "B) 90", "C) 100", "D) 110",
                "B) 90"
        ));

        questionList.add(new QuizQuestion(
                "3. (GK) गेटवे ऑफ इंडिया कहाँ स्थित है?",
                "A) दिल्ली", "B) आगरा", "C) जयपुर", "D) मुंबई",
                "D) मुंबई"
        ));

        questionList.add(new QuizQuestion(
                "4. (Geography) दुनिया का सबसे बड़ा महाद्वीप कौन सा है?",
                "A) यूरोप", "B) अफ्रीका", "C) एशिया", "D) ऑस्ट्रेलिया",
                "C) एशिया"
        ));

        questionList.add(new QuizQuestion(
                "5. (Fun) ऐसा कौन सा महीना है जिसमें लोग सबसे कम सोते हैं?",
                "A) जनवरी", "B) फरवरी", "C) मार्च", "D) दिसंबर",
                "B) फरवरी"
        ));

        Toast.makeText(this, "Rapid Fire: Quick Play Started! 🚀", Toast.LENGTH_SHORT).show();
    }
}