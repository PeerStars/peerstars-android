package com.peerstars.android.pstprofile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peerstars.android.R;
import com.peerstars.android.pstconfiguration.PSTConfiguration;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import java.io.File;

public class PSTCustomPersonalBookImageView extends ImageView implements ICallbacks {

    Context context;

    String type;
    String photoResizedUrl;
    String photoUrl;
    String thumbnailUrl;
    TextView txtActions;

    public PSTCustomPersonalBookImageView(Context context) {
        super(context);
        initTheView();
    }

    public PSTCustomPersonalBookImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTheView();
    }

    public PSTCustomPersonalBookImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTheView();
    }

    private void initTheView() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
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
            Bitmap image = (Bitmap) result;
            setImageBitmap(image);
        }
        if (result instanceof File) {
            setImageBitmap(BitmapFactory.decodeFile(((File) result).getAbsolutePath()));
        }
    }

    public synchronized void initContent(String type, String token, TextView actions, final Context context) {
        if (token.isEmpty())
            return;

        // set the context
        this.context = context;

        PSTClient request = new PSTClient(this, token, context);
        if (type.equals("image")) {
            if (this.photoResizedUrl.length() > 0) {
                request.getImageFromUrl(PSTConfiguration.getCurrentBucket(), this.photoResizedUrl);
            } else if (this.photoUrl.length() > 0) {
                request.getImageFromUrl(PSTConfiguration.getCurrentBucket(), this.photoUrl);
            }
        } else if (type.equals("video")) {
            request.getImageFromUrl(PSTConfiguration.getCurrentBucket(), this.thumbnailUrl);
        }
        this.txtActions = actions;
        this.txtActions.setClickable(true);
        this.txtActions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.feed_item_dropdown_menu);
                dialog.setTitle(PSTUtilities.getCustomFont("Manage Video", context));

                // set the custom dialog components - text, image and button
                TextView txtFlag = (TextView) dialog.findViewById(R.id.txtFlagAsInappropriate);
                txtFlag.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // dismiss the initial dialog
                        dialog.dismiss();

                        // custom dialog
                        final Dialog dialog2 = new Dialog(context);
                        dialog2.setContentView(R.layout.confirmation_yes_no);
                        dialog2.setTitle(PSTUtilities.getCustomFont("Flag as Inappropriate", context));
                        TextView btnConfirmYes = (TextView) dialog2.findViewById(R.id.btnConfirmYes);
                        btnConfirmYes.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });
                        TextView btnConfirmNo = (TextView) dialog2.findViewById(R.id.btnConfirmNo);
                        btnConfirmNo.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });

                        dialog2.show();
                    }
                });
                TextView txtAddToBook = (TextView) dialog.findViewById(R.id.txtAddToBook);
                txtAddToBook.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // dismiss the initial dialog
                        dialog.dismiss();

                        // custom dialog
                        final Dialog dialog2 = new Dialog(context);
                        dialog2.setContentView(R.layout.confirmation_yes_no);
                        dialog2.setTitle(PSTUtilities.getCustomFont("Add to your PeerBook?", context));
                        TextView txtConfirmText = (TextView) dialog2.findViewById(R.id.txtConfirmText);
                        txtConfirmText.setText("----------------");
                        TextView btnConfirmYes = (TextView) dialog2.findViewById(R.id.btnConfirmYes);
                        btnConfirmYes.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });
                        TextView btnConfirmNo = (TextView) dialog2.findViewById(R.id.btnConfirmNo);
                        btnConfirmNo.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });

                        dialog2.show();
                    }
                });
                TextView txtEditTags = (TextView) dialog.findViewById(R.id.txtEditTags);
                txtEditTags.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                TextView txtCancel = (TextView) dialog.findViewById(R.id.txtCancelFeedMenu);
                txtCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

            }
        });
    }

    public void setPhotoResizedUrl(String photoResizedUrl) {
        this.photoResizedUrl = photoResizedUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setDisplayType(String type) {
        this.type = type;
    }

}
