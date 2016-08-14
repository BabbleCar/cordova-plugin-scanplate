package com.tnc.alpr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class CustomEditText extends EditText {

    private CustomEditText me;

    public CustomEditText(Context context) {
        super(context);
        initialize();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public void initialize() {
        me = this;
        me.setFocusable(false);
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocusableInTouchMode(true);
                requestFocus();
                InputMethodManager keyboard=(InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(me,0);
            }
        });
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(EditorInfo.IME_ACTION_DONE == actionId) {
                    me.setFocusable(false);
                }

                return false;
            }
        });
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER) {
            me.setFocusable(false);
        }

        return super.onKeyPreIme(keyCode, event);
    }

}
