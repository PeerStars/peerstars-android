package com.peerstars.android.pstsignature;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstutilities.ICallbacks;

public class PSTCustomSignatureTextView extends TextView implements ICallbacks {

    // define widgets
    TextView btnAccept;
    TextView btnDecline;

    public PSTCustomSignatureTextView(Context context) {
        super(context);
        // Initialize the custom font
        init(null);
    }

    public PSTCustomSignatureTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Initialize the custom font
        init(attrs);
    }

    public PSTCustomSignatureTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Initailize the custom font
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode())
            return;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PSTCustomTextView);
            String fontName = a.getString(R.styleable.PSTCustomTextView_customFont);
            if (fontName != null) {
                Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
                setTypeface(myTypeface);
            }
            a.recycle();
        }
    }

    public void initContent(TextView btnAccept, TextView btnDecline, String token, final Context context) {

        // go get the widgets
        btnAccept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "This is an Accept test", Toast.LENGTH_LONG).show();
            }
        });
        btnDecline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "This is a Decline test", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void callbackProgress(int value) {

    }

    @Override
    public void callbackComplete() {
    }

    @Override
    public void callbackCompleteWithError(String error) {
    }

    @Override
    public void callbackResult(String result) {
    }

    @Override
    public void callbackResultObject(Object result) {
    }

}
