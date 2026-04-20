package com.example.onlineexamapp;

import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Base activity to provide shared functionality across the app,
 * such as automatic keyboard dismissal on tapping outside an input field.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Automatically hide keyboard when tapping outside of an EditText
        KeyboardUtils.handleTouchOutside(this, ev);
        return super.dispatchTouchEvent(ev);
    }
}
