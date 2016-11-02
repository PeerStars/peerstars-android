package com.peerstars.android.pstforgot_password;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PSTForgotPasswordActivity extends Activity implements ICallbacks {


    // Create the process flag
    PSTProcessEnum process = PSTProcessEnum.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

    }

    /*
  * Handle the Forgot password button click
  */
    public void btnSubmit_OnClick(View v) {

        // create a new request client
        final PSTClient request = new PSTClient(this, "", this);

        TextView txtSubmit = (TextView) findViewById(R.id.txtSubmit);
        txtSubmit.setBackgroundResource(R.drawable.transbackgroundblack);

        TextView txtEmailAddress = (TextView) findViewById(R.id.txtEmailAddress);

        // Submit the reset request
        JSONObject jsonObject;

        Map<String, String> data = new HashMap<>();
        data.put("email", txtEmailAddress.getText().toString());

        // Set the process to create account
        process = PSTProcessEnum.RESET_PASSWORD;

        try {
            // Setup and call the Simple Request object
            jsonObject = PSTUtilities.getJsonObjectFromMap(data);
            request.resetPassword(jsonObject.toString());
        } catch (JSONException je) {
            Log.d("Reset password failed!", je.getLocalizedMessage());
        }
    }

    @Override
    public void callbackProgress(int value) {
        Toast.makeText(getApplicationContext(), "Progress - " + value, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackComplete() {
        Toast.makeText(getApplicationContext(), "Your request has been sent. Check your email for instructions!", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void callbackCompleteWithError(String error) {
        if (error.contains("403")) {
            Toast.makeText(getApplicationContext(), "Forbidden", Toast.LENGTH_LONG).show();
        } else if (error.contains("404")) {
            Toast.makeText(getApplicationContext(), "Server not found!", Toast.LENGTH_LONG).show();
        } else if (error.contains("408")) {
            Toast.makeText(getApplicationContext(), "Process timed out, pleas try again later.", Toast.LENGTH_LONG).show();
        } else if (error.contains("409")) {
            Toast.makeText(getApplicationContext(), "User already exists, the email address must be unique!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void callbackResult(String result) {


    }

    @Override
    public void callbackResultObject(Object result) {

    }


    /*
     * Handle the Cancel button click
     */
    public void btnBack_OnClick(View v) {
        TextView txtCancel = (TextView) findViewById(R.id.txtCancel);
        txtCancel.setBackgroundResource(R.drawable.transbackgroundblack);
        finish();
    }

}
