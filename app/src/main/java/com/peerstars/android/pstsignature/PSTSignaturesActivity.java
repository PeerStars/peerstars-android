package com.peerstars.android.pstsignature;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.peerstars.android.R;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.pststorage.PSTMembersStorage;
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

public class PSTSignaturesActivity extends ActionBarActivity implements ICallbacks, SwipeRefreshLayout.OnRefreshListener {

    // create the video objects
    private VideoView homeFeedView;
    private int position = 0;
    private ProgressDialog progressDialog;
    private MediaController mediaControls;

    // create the shared preferences objects.
    SharedPreferences settings;
    SharedPreferences.Editor settingsHandler;

    // create all necessary objects
    String token = "";
    String groupId = "";
    String memberId = "";
    EditText name;

    // Create the process flag
    PSTProcessEnum process = PSTProcessEnum.IDLE;

    // create the GridView for the photos
    GridView gridViewPending;
    GridView gridViewApproved;

    // get the SwipeRefreshLayout
    LinearLayout pendingContainer;
    LinearLayout approvedContainer;

    // get the SwipeRefreshLayout
    SwipeRefreshLayout pendingLayout;
    SwipeRefreshLayout approvedLayout;

    // get the count views
    TextView pendingTxt;
    TextView approvedTxt;

    // build record counters
    int pendingCount = 0;
    int approvedCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signatures);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float density = metrics.density;
        int height = ConvertPixelsToDp(metrics.heightPixels, density);
        int width = ConvertPixelsToDp(metrics.widthPixels, density);

        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        // load the token
        token = settings.getString("token", "");

        // Initialize the image view
        pendingContainer = (LinearLayout) findViewById(R.id.pendingContainer);
        approvedContainer = (LinearLayout) findViewById(R.id.approvedContainer);
        pendingLayout = (SwipeRefreshLayout) findViewById(R.id.pendingLayout);
        pendingLayout.setOnRefreshListener(this);
        approvedLayout = (SwipeRefreshLayout) findViewById(R.id.approvedLayout);
        approvedLayout.setOnRefreshListener(this);
        gridViewPending = (GridView) findViewById(R.id.pendingGrid);
        gridViewPending.setAdapter(new PSTCustomSignatureAdapter(this));
        gridViewApproved = (GridView) findViewById(R.id.approvedGrid);
        gridViewApproved.setAdapter(new PSTCustomSignatureAdapter(this));
        pendingTxt = (TextView) findViewById(R.id.pendingTxt);
        approvedTxt = (TextView) findViewById(R.id.approvedTxt);

        // set the layout size
        ViewGroup.LayoutParams pendingParams = pendingContainer.getLayoutParams();
        ViewGroup.LayoutParams approvedParams = approvedContainer.getLayoutParams();

        pendingParams.height = height + 100;
        pendingContainer.setLayoutParams(pendingParams);
        approvedParams.height = height + 100;
        approvedContainer.setLayoutParams(approvedParams);

        // Set the title to Varsity font
        setTitle(PSTUtilities.getCustomFont("Signatures", getApplicationContext()));

        // get the group and member ids
        groupId = settings.getString("groupId", "");
        memberId = settings.getString("id", "");


        // load the signatures
        PSTClient request = new PSTClient(this, token, this);
        process = PSTProcessEnum.GET_SIGNATURES;
        request.loadSignatures(groupId, memberId);

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int ConvertPixelsToDp(float pixelValue, float density) {
        int dp = (int) ((pixelValue) / density);
        return dp;
    }

    @Override
    public void callbackProgress(int value) {

    }

    @Override
    public void callbackComplete() {
        if (process == PSTProcessEnum.GET_IMAGE) {
            // Toast.makeText(getApplicationContext(), "Get profile picture has completed!", Toast.LENGTH_LONG).show();
        }
        if (process == PSTProcessEnum.GET_PROFILE_ITEMS) {

        }
    }

    @Override
    public void callbackCompleteWithError(String error) {
        if (process == PSTProcessEnum.GET_IMAGE) {
            Toast.makeText(getApplicationContext(), "Get profile picture has failed with errors: " + error, Toast.LENGTH_LONG).show();
        }
        if (process == PSTProcessEnum.GET_PROFILE_ITEMS) {
            Toast.makeText(getApplicationContext(), "Get profile items has failed with errors: " + error, Toast.LENGTH_LONG).show();
        }
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
        } else {
            loadSignatures(result);
        }


        // stop the indicator
        pendingLayout.setRefreshing(false);
        approvedLayout.setRefreshing(false);
    }

    @Override
    public void callbackResultObject(Object result) {
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

    // load the feed
    private void loadSignatures(String values) {

        pendingCount = 0;
        approvedCount = 0;
        PSTCustomSignatureAdapter adaptorPending = (PSTCustomSignatureAdapter) gridViewPending.getAdapter();
        PSTCustomSignatureAdapter adaptorapproved = (PSTCustomSignatureAdapter) gridViewApproved.getAdapter();
        try {
            JSONArray mediaset = new JSONArray(values);
            if (mediaset.length() > 0) {
                adaptorPending.clearItems();
                adaptorapproved.clearItems();
            }
            for (int i = 0; i < mediaset.length(); i++)
                processMediaItemJSONObject(mediaset.getJSONObject(i), adaptorPending, adaptorapproved);
        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data: ", Toast.LENGTH_LONG);
        }
    }

    private void processMediaItemJSONObject(JSONObject mediaObject, PSTCustomSignatureAdapter adaptorPending, PSTCustomSignatureAdapter adaptorapproved) {

        String rc = "";
        String type = "";
        String status = "";
        String authorId = "";
        String content = "";
        String urlVideoThumb = "";
        String urlVideo360 = "";
        String urlVideo480 = "";
        String urlVideo720 = "";
        String urlVideo1080 = "";
        String memberId = "";
        String groupId = "";
        String sDate = null;
        String lifetime = "";

        try {
            // process the classes
            type = mediaObject.getString("type");
            status = mediaObject.getString("status");
            authorId = mediaObject.getString("authorId");
            memberId = mediaObject.getString("recipientId");
            groupId = mediaObject.getString("schoolId");
            sDate = mediaObject.getString("updatedAt");

            // get the urls for the definitions
            JSONArray media = mediaObject.getJSONArray("media");
            if (type.equals("text")) {
                content = mediaObject.getString("content");
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
            Boolean approved = Boolean.FALSE;
            if (status.toLowerCase().equals("approved"))
                approved = Boolean.TRUE;
            String[] params
                    = new String[]{
                    type,
                    status,
                    groupId,
                    authorId,
                    content,
                    urlVideoThumb,
                    urlVideo360,
                    urlVideo480,
                    urlVideo720,
                    urlVideo1080,
                    memberId,
                    lifetime,
                    approved.toString()};

            if (content.length() > 0 || urlVideoThumb.length() > 0) {
                if (!getMemberInfo(groupId, authorId))
                    return;
                if (status.toLowerCase().equals("approved")) {
                    rc = adaptorapproved.addItem(params, token);
                    approvedCount++;
                    approvedTxt.setText("Approved(" + String.valueOf(approvedCount) + ")");
                    adaptorapproved.notifyDataSetInvalidated();
                    gridViewApproved.invalidate();
                }
                if (status.toLowerCase().equals("pending")) {
                    rc = adaptorPending.addItem(params, token);
                    pendingCount++;
                    pendingTxt.setText("Pending(" + String.valueOf(pendingCount) + ")");
                    adaptorPending.notifyDataSetInvalidated();
                    gridViewPending.invalidate();
                }

            }
            if (rc.toLowerCase().equals("failed")) {
                // TODO: something
            }
        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data: " + je.getLocalizedMessage(), Toast.LENGTH_LONG);
        }

    }


    /*
     * Handle the accept button click event
     */
    public void btnAccept_OnClick(View v) {

    }

    @Override
    public void onRefresh() {

        // load the signatures
        PSTClient request = new PSTClient(this, token, this);
        process = PSTProcessEnum.GET_SIGNATURES;
        request.loadSignatures(groupId, memberId);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
        }
        return super.onKeyDown(keyCode, event);
    }
}
