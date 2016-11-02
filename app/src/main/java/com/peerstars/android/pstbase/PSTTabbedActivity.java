package com.peerstars.android.pstbase;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.peerstars.android.R;
import com.peerstars.android.pstcamera.PSTCameraActivity;
import com.peerstars.android.pstfeed.PSTFeedActivity;
import com.peerstars.android.pstportraits.PSTPortraitsActivity;
import com.peerstars.android.pstprofile.PSTProfileActivity;
import com.peerstars.android.pstsignature.PSTSignaturesActivity;

public class PSTTabbedActivity extends TabActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imgView;
    public static TabHost tabHost;

    // create the shared preferences accessor
    public SharedPreferences settings;
    public SharedPreferences.Editor settingsHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        Resources resources = getResources();
        tabHost = getTabHost();

        // create the shared preferences accessor
        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        // Home tab
        Intent intentHome = new Intent().setClass(this, PSTFeedActivity.class);
        TabSpec tabSpecHome = tabHost
                .newTabSpec("Home")
                .setIndicator("",
                        resources.getDrawable(R.drawable.home_icon96))
                .setContent(intentHome);
        // Portraits tab
        Intent intentPortraits = new Intent().setClass(this,
                PSTPortraitsActivity.class);
        TabSpec tabSpecPortraits = tabHost
                .newTabSpec("Portraits")
                .setIndicator("",
                        resources.getDrawable(R.drawable.portraits96))
                .setContent(intentPortraits);
        // Camera tab
        Intent intentCamera = new Intent().setClass(this, PSTCameraActivity.class);
        TabSpec tabSpecCamera = tabHost
                .newTabSpec("Camera")
                .setIndicator("",
                        resources.getDrawable(R.drawable.camera_button96))
                .setContent(intentCamera);
        // Signatures tab
        Intent intentSignatures = new Intent().setClass(this,
                PSTSignaturesActivity.class);
        TabSpec tabSpecSignatures = tabHost
                .newTabSpec("Signatures")
                .setIndicator("",
                        resources.getDrawable(R.drawable.signatures_button96))
                .setContent(intentSignatures);
        // Profile tab
        Intent intentProfile = new Intent().setClass(this,
                PSTProfileActivity.class);
        TabSpec tabSpecProfile = tabHost
                .newTabSpec("Profile")
                .setIndicator("",
                        resources.getDrawable(R.drawable.profile_button96))
                .setContent(intentProfile);
        // add all tabs
        tabHost.addTab(tabSpecHome);
        tabHost.addTab(tabSpecPortraits);
        tabHost.addTab(tabSpecCamera);
        tabHost.addTab(tabSpecSignatures);
        tabHost.addTab(tabSpecProfile);

        // set Windows tab as default (zero based)
        tabHost.setCurrentTab(0);

        getTabWidget().getChildAt(2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideTabs();
                tabHost.setCurrentTab(2);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                tabHost.getChildAt(0).dispatchKeyEvent(new KeyEvent(100, 100));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabbed, menu);
        return true;
    }

    // Show Tabs method
    public static void showTabs() {
        // set the camera widget to not show the nav bar
        tabHost.getTabWidget().getChildTabViewAt(0).setVisibility(View.VISIBLE);
        tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
        tabHost.getTabWidget().getChildTabViewAt(2).setVisibility(View.VISIBLE);
        tabHost.getTabWidget().getChildTabViewAt(3).setVisibility(View.VISIBLE);
        tabHost.getTabWidget().getChildTabViewAt(4).setVisibility(View.VISIBLE);
    }

    // Hide Tabs method
    public static void hideTabs() {
        // set the camera widget to not show the nav bar
        tabHost.getTabWidget().getChildTabViewAt(0).setVisibility(View.GONE);
        tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.GONE);
        tabHost.getTabWidget().getChildTabViewAt(2).setVisibility(View.GONE);
        tabHost.getTabWidget().getChildTabViewAt(3).setVisibility(View.GONE);
        tabHost.getTabWidget().getChildTabViewAt(4).setVisibility(View.GONE);
    }

    // Hide Tabs method
    public static void gotoFirstTab() {
        // set the camera widget to not show the nav bar
        tabHost.setCurrentTab(0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
        }
        return super.onKeyDown(keyCode, event);
    }
}