package com.peerstars.android.pstutilities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.pstportraits.PSTCustomGroupAdapter;
import com.peerstars.android.pstportraits.PSTCustomImageAdapter;

import org.json.JSONArray;

public class PSTSelectMemberActivity extends AppCompatActivity implements ICallbacks {

    // Did they select a group?
    boolean isSelected = false;

    // create a settings manager
    SharedPreferences settings;
    SharedPreferences.Editor settingsHandler;

    // hold the selected group id and name
    String groupId;
    String year;

    public AutoCompleteTextView actvGroupSelect;
    PSTCustomGroupAdapter adapter;

    private ProgressDialog progressDialog;

    private PSTProcessEnum process = PSTProcessEnum.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pstselect_member);

        // create a settings manager
        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        // set the screen title
        setTitle(PSTUtilities.getCustomFont("Compose", getApplicationContext()));

        // Setup and call the Simple Request object
        PSTClient request = new PSTClient(this, "", this);

        groupId = settings.getString("groupId", "");
        year = settings.getString("gradYear", "");
        progressDialog = ProgressDialog.show(PSTSelectMemberActivity.this, "Please wait ...", "Gathering some info ...", true);
        progressDialog.setCancelable(true);

        // Load the members
        process = PSTProcessEnum.GET_MEMBERS;
        request.loadMembersUrls(groupId, year);

    }

    @Override
    public void callbackProgress(int value) {

    }

    @Override
    public void callbackComplete() {
    }

    @Override
    public void callbackCompleteWithError(String error) {
        Toast.makeText(getApplicationContext(), "Load members has failed with errors: " + error, Toast.LENGTH_LONG).show();
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
        /*
        PSTMembersStorage.clear();
        try {
            JSONObject membersset = new JSONObject(members);
            JSONArray seniorsset = new JSONArray(membersset.getString("seniors"));
            JSONArray juniorsset = new JSONArray(membersset.getString("juniors"));
            JSONArray sophomoresset = new JSONArray(membersset.getString("sophomores"));
            JSONArray freshmenset = new JSONArray(membersset.getString("freshmen"));

            PSTCustomImageAdapter adaptor = (PSTCustomImageAdapter)gridView.getAdapter();
            if (seniorsset.length()<0||juniorsset.length()>0||sophomoresset.length()>0||freshmenset.length()>0)
                adaptor.clearItems();
            if(seniorsset.length()>0)
                processSubGroup(seniorsset, adaptor);
            if(juniorsset.length()>0)
                processSubGroup(juniorsset, adaptor);
            if(sophomoresset.length()>0)
                processSubGroup(sophomoresset, adaptor);
            if(freshmenset.length()>0)
                processSubGroup(freshmenset, adaptor);

        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data", Toast.LENGTH_LONG);
        }
        */
    }

    private void processSubGroup(JSONArray subgroup, PSTCustomImageAdapter adaptor) {

        /*
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
        }catch (JSONException je){
            //TODO: dsomething
        }
        */
    }

    public void btnCancel_OnClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void btnNext_OnClick(View v) {

    }
}

