package com.peerstars.android.pstutilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.peerstars.android.R;

public class PSTCustomTextView extends TextView {

    public PSTCustomTextView(Context context) {
        super(context);
        // Initailize the custom font
        init(null);
    }

    public PSTCustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Initailize the custom font
        init(attrs);
    }

    public PSTCustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
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

}
