package com.peerstars.android.pstcreate_account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstportraits.PSTCustomGroupAdapter;
import com.peerstars.android.pststorage.PSTDatabaseTableGroups;
import com.peerstars.android.pststorage.PSTGroup;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

public class PSTSelectGroupActivity extends AppCompatActivity implements ICallbacks {

    // Did they select a group?
    boolean isSelected = false;

    // hold the selected group id and name
    String groupId;
    String groupName;

    public AutoCompleteTextView actvGroupSelect;
    PSTCustomGroupAdapter adapter;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);

        setTitle(PSTUtilities.getCustomFont("Create Account", getApplicationContext()));

        // Setup and call the Simple Request object
        PSTClient request = new PSTClient(this, "", this);

        progressDialog = ProgressDialog.show(PSTSelectGroupActivity.this, "Please wait ...", "Gathering some info ...", true);
        progressDialog.setCancelable(true);

        // Load the groups
        request.GetGroups();

    }

    @Override
    public void callbackProgress(int value) {

    }

    @Override
    public void callbackComplete() {
    }

    @Override
    public void callbackCompleteWithError(String error) {
        Toast.makeText(getApplicationContext(), "Load groups has failed with errors: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackResult(String result) {
        //Toast.makeText(getApplicationContext(), "Results: " + result, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackResultObject(Object result) {

        // get the select textview
        actvGroupSelect = (AutoCompleteTextView) findViewById(R.id.actvGroupSelect);

        // Set the dropdown adapter
        adapter = new PSTCustomGroupAdapter(getApplicationContext(), (PSTDatabaseTableGroups) result);
        actvGroupSelect.setAdapter(adapter);
        actvGroupSelect.setThreshold(2);
        actvGroupSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PSTGroup group = (PSTGroup) adapterView.getItemAtPosition(position);
                actvGroupSelect.setText(group.getName());
                groupId = String.valueOf(group.getId());
                groupName = group.getName();
                isSelected = true;
            }
        });

        // dismiss the progress dialog
        progressDialog.cancel();
    }

    public void btnBack_OnClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void btnDone_OnClick(View v) {
        if (isSelected) {
            Intent data = new Intent();
            data.setData(Uri.parse(groupId + "," + groupName));
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
