package com.peerstars.android.pstportraits;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peerstars.android.pstconfiguration.PSTConfiguration;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

public class PSTCustomImageView extends ImageView implements ICallbacks, View.OnClickListener {

    String gradYear;
    String photoUrl;
    public String type;
    TextView txtActions;

    public PSTCustomImageView(Context context) {
        super(context);
    }

    public PSTCustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PSTCustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight()); //Snap to width --- was width and width
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
        if (result instanceof Bitmap) {
            Bitmap image;
            if (this.type.equals("roundBitmap")) {
                image = PSTUtilities.getRoundedShape((Bitmap) result);
            } else {
                image = (Bitmap) result;
            }
            setImageBitmap(image);
        }
    }


    public void setUrls(
            String gradYear,
            String photoUrl) {
        setGradYear(gradYear);
        setPhotoUrl(photoUrl);
    }

    public void initContent(String type, String token) {
        this.type = type;
        try {
            PSTClient request = new PSTClient(this, token, getContext());
            request.getImageFromUrl(PSTConfiguration.getCurrentBucket(), this.photoUrl);
        } catch (Exception e) {
        }
    }

    public void setGradYear(String gradYear) {
        this.gradYear = gradYear;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public void onClick(View v) {
        String test = "Test";
    }

    public void onActions_Click(View v) {
        String test = "Test";
    }
}
