package com.peerstars.android.pstfeed;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.peerstars.android.R;
import com.peerstars.android.pstconfiguration.PSTConfiguration;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.psttags.PSTEditTagsActivity;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONObject;

public class PSTCustomFeedImageView extends ImageView implements ICallbacks, View.OnClickListener {

    Context context;

    String id = "";
    String memberId = "";
    String groupId = "";

    String type;
    String photoResizedUrl;
    String photoUrl;
    TextView txtActions;
    TextView txtFlagged;
    String tags;

    // Create the process flag
    PSTProcessEnum process = PSTProcessEnum.IDLE;

    // build the image bitmap
    Bitmap image;

    // get the progress bar
    ProgressBar progressBar;

    // Create an ownedBy boolean
    Boolean ownedBy = false;

    public PSTCustomFeedImageView(Context context) {
        super(context);
        initTheView();
    }

    public PSTCustomFeedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTheView();
    }

    public PSTCustomFeedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTheView();
    }

    private void initTheView() {

        // set the image place holder
        //setImageResource(R.drawable.ic_launcher);
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
        progressBar.setVisibility(INVISIBLE);
    }

    @Override
    public void callbackCompleteWithError(String error) {
        progressBar.setVisibility(INVISIBLE);
    }

    @Override
    public void callbackResult(String result) {

    }

    @Override
    public void callbackResultObject(Object result) {
        if (result instanceof Bitmap) {
            image = (Bitmap) result;
            setImageBitmap(image);
        }
        progressBar.setVisibility(INVISIBLE);
    }

    public synchronized void initContent(String type, final String token, TextView actions, final Boolean ownedBy, TextView flaggedView, final String tags, ProgressBar progressBar, String flagged, final Context context) {
        if (token.isEmpty())
            return;

        // hold the tags
        this.tags = tags;

        // set the context
        this.context = context;

        // do i own this item?
        this.ownedBy = ownedBy;

        // show the progress circle
        this.progressBar = progressBar;
        progressBar.setVisibility(VISIBLE);

        // is it flagged?
        this.txtFlagged = flaggedView;
        this.txtFlagged.setVisibility(INVISIBLE);
        if (!flagged.equals("0"))
            this.txtFlagged.setVisibility(VISIBLE);

        // create a new request client
        final PSTClient request = new PSTClient(this, token, getContext());
        progressBar.setVisibility(VISIBLE);

        if (this.photoResizedUrl.length() > 0) {
            request.getImageFromUrl(PSTConfiguration.getCurrentBucket(), this.photoResizedUrl);
        } else if (this.photoUrl.length() > 0) {
            request.getImageFromUrl(PSTConfiguration.getCurrentBucket(), this.photoUrl);
        }
        this.txtActions = actions;
        this.txtActions.setClickable(true);
        this.txtActions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // custom dialog
                final Dialog dialog = new Dialog(context);
                if (ownedBy) {
                    dialog.setContentView(R.layout.feed_owned_item_dropdown_menu);
                } else {
                    dialog.setContentView(R.layout.feed_item_dropdown_menu);
                }
                dialog.setTitle(PSTUtilities.getCustomFont("Manage Photo", context));

                // find the menu selections for ownedBy
                TextView txtFlag = (TextView) dialog.findViewById(R.id.txtFlagAsInappropriate);
                TextView txtAddToBook = (TextView) dialog.findViewById(R.id.txtAddToBook);
                TextView txtDelete = (TextView) dialog.findViewById(R.id.txtDelete);

                if (!ownedBy) {
                    // set the custom dialog components - text, image and button
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

                                    JSONObject userdata = null;

                                    dialog.dismiss();

                                    // Set the process to create account
                                    process = PSTProcessEnum.FLAG_CONTENT;

                                    // Setup and call the Simple Request object
                                    request.flagAsInappropriate(groupId, memberId, id, token);

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
                } else {
                    txtDelete.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            // dismiss the initial dialog
                            dialog.dismiss();

                            // custom dialog
                            final Dialog dialog2 = new Dialog(context);
                            dialog2.setContentView(R.layout.confirmation_yes_no);
                            dialog2.setTitle(PSTUtilities.getCustomFont("Delete Entry", context));
                            TextView btnConfirmYes = (TextView) dialog2.findViewById(R.id.btnConfirmYes);
                            btnConfirmYes.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    dialog2.dismiss();

                                    JSONObject userdata = null;

                                    dialog.dismiss();

                                    // Set the process to create account
                                    process = PSTProcessEnum.FLAG_CONTENT;

                                    // Setup and call the Simple Request object
                                    request.deleteEntry(groupId, memberId, id, token);

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
                }
                TextView txtEditTags = (TextView) dialog.findViewById(R.id.txtEditTags);
                txtEditTags.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(context, PSTEditTagsActivity.class);
                        intent.putExtra("tags", tags);
                        intent.putExtra("id", id);
                        ((Activity) context).startActivityForResult(intent, 1);
                        dialog.dismiss();

                    }
                });
                TextView txtShare = (TextView) dialog.findViewById(R.id.txtEditTags);
                txtShare.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(context, PSTEditTagsActivity.class);
                        intent.putExtra("tags", tags);
                        intent.putExtra("id", id);
                        ((Activity) context).startActivityForResult(intent, 1);
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

        this.setClickable(true);
        this.setOnClickListener(this);
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

    public void setIds(String id, String memberId, String groupId) {
        this.id = id;
        this.memberId = memberId;
        this.groupId = groupId;
    }

    public void setImageProgressBar(ProgressBar bar) {
        progressBar = bar;
    }

    @Override
    public void onClick(View v) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(image);

        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(imageView);
        dialog.show();
    }

}
