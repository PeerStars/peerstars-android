package com.peerstars.android.pstcamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.peerstars.android.R;
import com.peerstars.android.psttags.PSTEditTagsActivity;

public class PSTViewContentActivity extends Activity {

    public static int RESULT_GET_TAGS = 0;
    TextView btnCancel;
    TextView btnAddTags;
    TextView btnAccept;
    CheckBox privateCBox;
    String fileType;
    String filePath;

    ImageView imagePreview;
    VideoView videoPreview;
    String tags = "";
    String tagIds = "";

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_content);

        // instantiate the objects
        btnCancel = (TextView) findViewById(R.id.btnCancel);
        btnAddTags = (TextView) findViewById(R.id.btnAddTags);
        btnAccept = (TextView) findViewById(R.id.btnAccept);
        privateCBox = (CheckBox) findViewById(R.id.privateCBox);
        progressBar = (ProgressBar) findViewById(R.id.uploadProgressBar);

        imagePreview = (ImageView) findViewById(R.id.imagePreview);
        videoPreview = (VideoView) findViewById(R.id.videoPreview);

        imagePreview.setVisibility(View.INVISIBLE);
        videoPreview.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();

        fileType = bundle.getString("type");
        filePath = bundle.getString("path");
        if (fileType != null) {
            // What type do we have?
            if (fileType.equals("image")) {
                setImagePreviewContent(filePath);
            } else {
                setVideoPreviewContent(filePath);
            }

        }

    }

    public void setImagePreviewContent(String path) {
        Bitmap image = BitmapFactory.decodeFile(path);
        videoPreview.setVisibility(View.INVISIBLE);
        videoPreview.setVideoPath("");
        imagePreview.setImageBitmap(image);
        imagePreview.setVisibility(View.VISIBLE);
    }

    public void setVideoPreviewContent(String path) {
        imagePreview.setVisibility(View.INVISIBLE);
        imagePreview.setImageBitmap(null);
        videoPreview.setVideoPath(path);
        videoPreview.setVisibility(View.VISIBLE);
        MediaController mediaController = new
                MediaController(this);
        mediaController.setAnchorView(videoPreview);
        videoPreview.setMediaController(mediaController);
        videoPreview.start();
    }

    public void btnCancel_OnClick(View v) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    public void btnAddTags_OnClick(View v) {
        Intent i = new Intent(this, PSTEditTagsActivity.class);
        i.putExtra("tags", "");
        i.putExtra("id", 0);
        startActivityForResult(i, 0);
    }

    public void btnAccept_OnClick(View v) {
        btnAccept.setBackgroundResource(R.drawable.transbackgroundblack);
        btnAccept.setEnabled(false);
        btnAddTags.setEnabled(false);
        btnCancel.setEnabled(false);
        imagePreview.setEnabled(false);
        videoPreview.setEnabled(false);

        // Is the file public or private?
        int secret = 0;
        if (privateCBox.isChecked())
            secret = 1;

        // Return all data to the camera activity
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_OK, returnIntent);
        returnIntent.putExtra("tags", tags);
        returnIntent.putExtra("tagIds", tagIds);
        returnIntent.putExtra("secret", secret);
        returnIntent.putExtra("path", filePath);

        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_GET_TAGS && resultCode == RESULT_OK && null != data) {
            tags = data.getStringExtra("result");
            tagIds = data.getStringExtra("tagIds");
        }
    }

}
