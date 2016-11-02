package com.peerstars.android.pstfeed;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.peerstars.android.R;

public class PSTImageFullScreenActivity extends AppCompatActivity {

    // create a bitmap file for the display
    Bitmap photo;
    String pictureUrl;

    // create a picture image view
    ImageView picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pstimage_full_screen);
        picture = (ImageView) findViewById(R.id.pictureView);

        // load the picture
        Bundle extras = getIntent().getExtras();
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inSampleSize = 2;
        pictureUrl = extras.getString("pictureUrl");
        photo = BitmapFactory.decodeFile(pictureUrl, bfo);
        picture.setImageBitmap(photo);
    }

}
