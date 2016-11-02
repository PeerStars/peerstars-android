package com.peerstars.android.pstfeed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstauthentication.PSTAuthenticatedUser;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.pststorage.PSTMembersStorage;
import com.peerstars.android.pststorage.PSTTagsStorage;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PSTFeedActivity extends AppCompatActivity implements ICallbacks, SwipeRefreshLayout.OnRefreshListener {

    // create the shared preferences accessor
    public SharedPreferences settings;
    public SharedPreferences.Editor settingsHandler;

    private PSTAuthenticatedUser authUser = new PSTAuthenticatedUser();

    private String user = "";
    private String password = "";
    private String groupId = "";
    private String filters = "";
    private String year = "";
    private String token = "";

    // create the process flag
    PSTProcessEnum process = PSTProcessEnum.IDLE;

    // create the GridView for the photos
    GridView gridView;

    // Setup and call the Simple Request object
    PSTClient request;

    // get the SwipeRefreshLayout
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // create the shared preferences accessor
        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        // Set the title to Varsity font
        setTitle(PSTUtilities.getCustomFont(settings.getString("name", ""), getApplicationContext()));

        // Initialize the image view
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        gridView = (GridView) findViewById(R.id.gridViewFeed);
        gridView.setAdapter(new PSTCustomFeedAdapter(this));

        user = settings.getString("user", "");
        password = settings.getString("password", "");
        token = settings.getString("token", "");
        authUser.setUser(user);
        authUser.setPassword(password);
        authUser.setToken(token);

        // Setup and call the Simple Request object
        request = new PSTClient(this, token, this);

        // Show Progress Dialog
        swipeRefreshLayout.setRefreshing(true);

        // load the necessary objects
        year = settings.getString("yearEnd", "");
        groupId = settings.getString("groupId", "");
        token = settings.getString("token", "");

        // Get the Tags
        process = PSTProcessEnum.GET_TAGS;
        request.loadTags(token, groupId);
        // dismiss the progress dialog
        //prgDialog.cancel();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void callbackProgress(int value) {

    }

    @Override
    public void callbackComplete() {
        // Toast.makeText(getApplicationContext(), "Get user data has completed!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackCompleteWithError(String error) {
        Toast.makeText(getApplicationContext(), "Load feed urls has failed with errors: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackResult(String result) {
        if (process == PSTProcessEnum.GET_MEMBER) {
            try {
                JSONObject member = new JSONObject(result);

                PSTMembersStorage.addMember(
                        member.getInt("id"), member.getString("firstName"),
                        member.getString("lastName"),
                        String.valueOf(member.getInt("gradYear")),
                        member.getString("photoUrl"));

                onRefresh();

            } catch (Exception e) {
            }
        }

        if (process == PSTProcessEnum.GET_FEED_URLS) {
            loadTheFeed(result);
        }
        if (process == PSTProcessEnum.GET_TAGS) {
            PSTTagsStorage.setTagsJson(result);

            // Load the feed
            filters = "feed";
            process = PSTProcessEnum.GET_FEED_URLS;
            request.loadFeedUrls(groupId, year, filters, "25");

        }
        if (process == PSTProcessEnum.SET_TAGS) {
            onRefresh();
        }
    }

    @Override
    public void callbackResultObject(Object result) {
        // Toast.makeText(getApplicationContext(), "Get user data has returned an object: " + result.toString(), Toast.LENGTH_LONG);
    }

    // load the feed
    private void loadTheFeed(String values) {

        PSTCustomFeedAdapter adaptor = (PSTCustomFeedAdapter) gridView.getAdapter();
        adaptor.setMemberId(settings.getString("id", ""));
        try {
            JSONArray mediaSet = new JSONArray(values);
            if (mediaSet.length() > 0)
                adaptor.clearItems();
            for (int i = 0; i < mediaSet.length(); i++)
                processMediaItemJSONObject(mediaSet.getJSONObject(i), adaptor);
        } catch (JSONException je) {
            //Toast.makeText(getApplicationContext(), "Invalid JSON data: ", Toast.LENGTH_LONG).show();
        }

        // stop the indicator
        swipeRefreshLayout.setRefreshing(false);

    }

    private Boolean getMemberInfo(String groupId, String memberId) {
        if (PSTMembersStorage.getMember(Integer.parseInt(memberId)) == null) {
            PSTClient request = new PSTClient(this, "", this);
            process = PSTProcessEnum.GET_MEMBER;
            request.getMember(groupId, memberId, token);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private void processMediaItemJSONObject(JSONObject mediaObject, PSTCustomFeedAdapter adaptor) {

        String rc = "";
        StringBuilder tagBuilder = new StringBuilder();
        String id = "";
        String type = "";
        String urlStandard = "";
        String urlResized = "";
        String urlVideoThumb = "";
        String urlVideo360 = "";
        String urlVideo480 = "";
        String urlVideo720 = "";
        String urlVideo1080 = "";
        String memberId = "";
        String groupId = "";
        String sDate = null;
        String lifetime = "";
        String flagged = "0";

        try {
            // process the classes
            id = mediaObject.getString("id");
            type = mediaObject.getString("type");
            memberId = mediaObject.getString("studentId");
            groupId = mediaObject.getString("schoolId");
            sDate = mediaObject.getString("updatedAt");
            flagged = mediaObject.getString("flagCount");

            // get the tags
            JSONArray tags = mediaObject.getJSONArray("tags");
            for (int i = 0; i < tags.length(); i++) {
                JSONObject tag = tags.getJSONObject(i);
                tagBuilder.append("#" + tag.getString("name") + " ");
            }

            // get the urls for the definitions
            JSONArray media = mediaObject.getJSONArray("media");
            if (type.equals("image")) {
                for (int i = 0; i < media.length(); i++) {
                    if (media.getJSONObject(i).getString("path").contains("resized")) {
                        urlResized = media.getJSONObject(i).getString("path");
                    } else {
                        urlStandard = media.getJSONObject(i).getString("path");
                    }
                }
            } else {
                for (int i = 0; i < media.length(); i++) {
                    // get the JSONObject and parse it
                    JSONObject jo = media.getJSONObject(i);
                    String path = jo.getString("path");
                    if (path.contains("thumbnails")) {
                        urlVideoThumb = path;
                    } else if (path.contains("360p")) {
                        urlVideo360 = path;
                    } else if (path.contains("480p")) {
                        urlVideo480 = path;
                    } else if (path.contains("720p")) {
                        urlVideo720 = path;
                    } else if (path.contains("1080p")) {
                        urlVideo1080 = path;
                    }
                }
            }

            try {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
                Date dateUpdated = formatter.parse(sDate.replace("Z", ""));
                Calendar cal = Calendar.getInstance();
                Calendar calNow = Calendar.getInstance();
                cal.setTime(dateUpdated);
                long lt = (calNow.getTimeInMillis() - cal.getTimeInMillis()) / (86400 * 1000);
                lt = lt - (lt % 1);
                lifetime = lt + "d";
                if (lt >= 7) {
                    lt = lt / 7;
                    lt = lt - (lt % 1);
                    lifetime = lt + "w";
                }
            } catch (Exception ce) {
                Log.d("Date formatting error", ce.getMessage());
            }
            String[] params
                    = new String[]{
                    id,
                    memberId,
                    groupId,
                    type,
                    urlStandard,
                    urlResized,
                    urlVideoThumb,
                    urlVideo360,
                    urlVideo480,
                    urlVideo720,
                    urlVideo1080,
                    tagBuilder.toString(),
                    memberId,
                    groupId,
                    lifetime,
                    flagged};

            if (urlResized.length() > 0 || urlVideoThumb.length() > 0) {
                if (!getMemberInfo(groupId, memberId))
                    return;
                rc = adaptor.addItem(params, token);
                adaptor.notifyDataSetInvalidated();
                gridView.invalidate();
            }
            if (rc.toLowerCase().equals("failed")) {
                // TODO: something
            }
        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data: " + je.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                String id = data.getStringExtra("id");
                String tagIds = data.getStringExtra("tagIds");
                groupId = settings.getString("groupId", "");
                String memberId = settings.getString("id", "");
                token = settings.getString("token", "");
                process = PSTProcessEnum.SET_TAGS;
                request.setTags(groupId, memberId, id, tagIds, token);
            }
        }
    }//onActivityResult

    @Override
    public void onRefresh() {

        // Show Progress Dialog
        //prgDialog.show();

        // Load the feed
        year = settings.getString("yearEnd", "");
        groupId = settings.getString("groupId", "");
        token = settings.getString("token", "");
        filters = "feed";
        process = PSTProcessEnum.GET_FEED_URLS;
        PSTCustomFeedAdapter adapter = (PSTCustomFeedAdapter) gridView.getAdapter();
        adapter.clearItems();
        request.loadFeedUrls(groupId, year, filters, "25");

        // dismiss the progress dialog
        //prgDialog.cancel();

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == 100) {
            if (settings.getBoolean("refresh", false)) {
                settingsHandler.putBoolean("refresh", false);
                settingsHandler.commit();
                onRefresh();
            }
            return true;
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
        }
        if ((keyCode == 100)) {
            Log.d(this.getClass().getName(), "Refresh view...");
            onRefresh();
        }
        return super.onKeyDown(keyCode, event);
    }
}
