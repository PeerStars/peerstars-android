package com.peerstars.android.pstfeed;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
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

import java.io.File;
import java.io.IOException;

public class PSTCustomFeedVideoView extends TextureView implements ICallbacks, TextureView.SurfaceTextureListener, View.OnClickListener {

    Context context;

    String id = "";
    String memberId = "";
    String groupId = "";

    String type;
    String thumbnailUrl;
    String video360Url;
    String video480Url;
    String video720Url;
    String video1080Url;

    File videoFile;
    Bitmap thumb;
    ImageView thumbView;
    ImageView videoicon;

    // create a process flag
    PSTProcessEnum process;

    TextView txtActions;
    TextView txtFlagged;
    String tags = "";

    // create the progress bar
    ProgressBar progressBar;

    // Create an ownedBy boolean
    Boolean ownedBy = false;

    // Log tag.
    private static final String TAG = PSTCustomFeedImageView.class.getName();

    // MediaPlayer instance to control playback of video file.
    private MediaPlayer mMediaPlayer;

    public PSTCustomFeedVideoView(Context context) {
        super(context);

        // listen for surface texture available
        setSurfaceTextureListener(this);

        // set the image place holder
        thumbView.setImageResource(R.drawable.peersbgtemp);

    }

    public PSTCustomFeedVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // listen for surface texture available
        setSurfaceTextureListener(this);

    }

    public PSTCustomFeedVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // listen for surface texture available
        setSurfaceTextureListener(this);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = (int) (width * .75);
        setMeasuredDimension(width, height); //Snap to width --- was width and width
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
        if (result instanceof File) {
            videoFile = (File) result;
        }
        if (result instanceof Bitmap) {
            thumb = (Bitmap) result;
            thumbView.setImageBitmap(thumb);
            thumbView.setVisibility(VISIBLE);
        }
    }

    public void setUrls(
            String thumbnailUrl,
            String video360Url,
            String video480Url,
            String video720Url,
            String video1080Url) {
        setThumbnailUrl(thumbnailUrl);
        setVideo360Url(video360Url);
        setVideo480Url(video480Url);
        setVideo720Url(video720Url);
        setVideo1080Url(video1080Url);
    }

    public synchronized void initContent(String type, final String token, TextView actions, final Boolean ownedBy, TextView flaggedView, ImageView videoicon, final String tags, ProgressBar progressBar, String flagged, final Context context) {
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

        // set the process to get image
        process = PSTProcessEnum.GET_IMAGE;
        final PSTClient request = new PSTClient(this, token, context);
        request.getImageFromUrl(PSTConfiguration.getCurrentBucket(), this.thumbnailUrl);
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

        this.videoicon = videoicon;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setVideo360Url(String video360Url) {
        this.video360Url = video360Url;
    }

    public void setVideo480Url(String video480Url) {
        this.video480Url = video480Url;
    }

    public void setVideo720Url(String video720Url) {
        this.video720Url = video720Url;
    }

    public void setVideo1080Url(String video1080Url) {
        this.video1080Url = video1080Url;
    }

    public void setDisplayType(String type) {
        this.type = type;
    }

    public void setThumbView(ImageView thumbView) {
        this.thumbView = thumbView;
    }

    public void setActions(TextView actions) {
        this.txtActions = actions;
    }

    public void setImageProgressBar(ProgressBar bar) {
        progressBar = bar;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {

        Surface surface = new Surface(surfaceTexture);

        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            String url = PSTConfiguration.getAWSCurrentURL() + video1080Url;
            mMediaPlayer.setDataSource(getContext(), Uri.parse(url));

            mMediaPlayer.setSurface(surface);
            //mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();

            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    //progressDialog.cancel();
                    mediaPlayer.start();
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                }
            });

            // create an error handler for the media player
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // ... react appropriately ...
                    // The MediaPlayer has moved to the Error state, must be reset!
                    mMediaPlayer.reset();
                    Log.e(TAG, "onError - media player crash: " + what);
                    return true;
                }
            });

            // create a completed listener for the media player
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    thumbView.setVisibility(VISIBLE);
                    videoicon.setVisibility(VISIBLE);
                }
            });

        } catch (IllegalArgumentException e) {
            try {
                Log.d(TAG, e.getMessage());
            } catch (Exception e1) {
            }
        } catch (SecurityException e) {
            try {
                Log.d(TAG, e.getMessage());
            } catch (Exception e1) {
            }
        } catch (IllegalStateException e) {
            try {
                Log.d(TAG, e.getMessage());
            } catch (Exception e1) {
            }
        } catch (IOException e) {
            try {
                Log.d(TAG, e.getMessage());
            } catch (Exception e1) {
            }
        }

        setClickable(true);
        setOnClickListener(this);

        thumbView.setVisibility(VISIBLE);
        thumbView.setClickable(true);
        thumbView.setOnClickListener(this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onClick(View v) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            thumbView.setVisibility(VISIBLE);
            videoicon.setVisibility(VISIBLE);
        } else {
            mMediaPlayer.start();
            int i = 0;
            while (!mMediaPlayer.isPlaying()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                if (i++ > 30)
                    break;
            }
            thumbView.setVisibility(GONE);
            videoicon.setVisibility(GONE);
        }
    }

    public void setIds(String id, String memberId, String groupId) {
        this.id = id;
        this.memberId = memberId;
        this.groupId = groupId;
    }


}
