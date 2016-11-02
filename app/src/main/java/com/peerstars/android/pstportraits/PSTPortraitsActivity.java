package com.peerstars.android.pstportraits;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.MediaController;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pststorage.PSTMembersStorage;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class PSTPortraitsActivity extends ActionBarActivity implements ICallbacks {

    // create the video objects
    private ProgressDialog progressDialog;
    private MediaController mediaControls;

    // create the shared preferences accessor
    public SharedPreferences settings;
    public SharedPreferences.Editor settingsHandler;

    // create the GridView for the photos
    GridView gridView;

    // Create a token object
    String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portraits);

        setTitle(PSTUtilities.getCustomFont("Peers", getApplicationContext()));

        // create the shared preferences accessor
        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        token = settings.getString("token", "");

        // Initialize the image view
        gridView = (GridView) findViewById(R.id.gridViewPortraits);
        gridView.setAdapter(new PSTCustomImageAdapter(this.getApplicationContext()));

        // create the progress bar while the video file loads
        progressDialog = new ProgressDialog(this);
        // set a title for the progress bar
        progressDialog.setTitle("Video File");
        // set the message for the progress bar
        progressDialog.setMessage("Loading...");
        // set the progress bar to "Not Cancelable"
        progressDialog.setCancelable(false);

        // Setup and call the Simple Request object
        PSTClient request = new PSTClient(this, token, this);

        // Show Progress Dialog
        //prgDialog.show();

        // get the current school year end
        Calendar today = Calendar.getInstance();
        Calendar yearEnd = Calendar.getInstance();
        yearEnd.set(today.get(Calendar.YEAR), 6, 30);
        String year = String.valueOf(today.get(Calendar.YEAR));
        if (today.after(yearEnd)) {
            year = String.valueOf(today.get(Calendar.YEAR) + 1);
        }
        String group = settings.getString("groupId", "");
        // Load the feed
        request.loadMembersUrls(group, year);

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Implement callback methods

    @Override
    public void callbackProgress(int value) {

    }

    @Override
    public void callbackComplete() {
        //Toast.makeText(getApplicationContext(), "Get members has completed!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackCompleteWithError(String error) {
        Toast.makeText(getApplicationContext(), "Get members has failed with errors: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackResult(String result) {
        //Toast.makeText(getApplicationContext(), "Results: " + result, Toast.LENGTH_LONG).show();
        processMembers(result);
    }

    @Override
    public void callbackResultObject(Object result) {
        //Toast.makeText(getApplicationContext(), "Get user data has returned an object: " + result.toString(), Toast.LENGTH_LONG);
    }

    private void processMembers(String members) {
        String rc = "";
        PSTMembersStorage.clear();
        try {
            JSONObject membersset = new JSONObject(members);
            JSONArray seniorsset = new JSONArray(membersset.getString("seniors"));
            JSONArray juniorsset = new JSONArray(membersset.getString("juniors"));
            JSONArray sophomoresset = new JSONArray(membersset.getString("sophomores"));
            JSONArray freshmenset = new JSONArray(membersset.getString("freshmen"));

            PSTCustomImageAdapter adaptor = (PSTCustomImageAdapter) gridView.getAdapter();
            if (seniorsset.length() < 0 || juniorsset.length() > 0 || sophomoresset.length() > 0 || freshmenset.length() > 0)
                adaptor.clearItems();
            if (seniorsset.length() > 0)
                processSubGroup(seniorsset, adaptor);
            if (juniorsset.length() > 0)
                processSubGroup(juniorsset, adaptor);
            if (sophomoresset.length() > 0)
                processSubGroup(sophomoresset, adaptor);
            if (freshmenset.length() > 0)
                processSubGroup(freshmenset, adaptor);

        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data", Toast.LENGTH_LONG);
        }
    }

    private void processSubGroup(JSONArray subgroup, PSTCustomImageAdapter adaptor) {

        String rc = "";
        try {
            // process the classes
            for (int i = 0; i < subgroup.length(); i++) {
                JSONObject member = subgroup.getJSONObject(i);
                String[] params
                        = new String[]{
                        member.getString("id"),
                        member.getString("firstName"),
                        member.getString("lastName"),
                        String.valueOf(member.getInt("gradYear")),
                        member.getString("photoUrl"),
                };
                PSTMembersStorage.addMember(
                        member.getInt("id"),
                        member.getString("firstName"),
                        member.getString("lastName"),
                        String.valueOf(member.getInt("gradYear")),
                        member.getString("photoUrl"));
                rc = adaptor.addItem(params, token);
                adaptor.notifyDataSetInvalidated();
                gridView.invalidate();
                if (rc.toLowerCase().equals("failed")) {
                    // TODO: something
                }
            }
        } catch (JSONException je) {
            //TODO: dsomething
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
        }
        return super.onKeyDown(keyCode, event);
    }
}
