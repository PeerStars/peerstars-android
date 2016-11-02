package com.peerstars.android.pststartup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstconfiguration.PSTConfiguration;
import com.peerstars.android.pstcreate_account.PSTCreateAccountActivity;
import com.peerstars.android.pstforgot_password.PSTForgotPasswordActivity;
import com.peerstars.android.pstlogin.PSTLoginActivity;
import com.peerstars.android.pststorage.PSTStorageHandler;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

public class PSTStartupActivity extends Activity implements ICallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // set the app context
        PSTStorageHandler.setContext(getApplicationContext());
        PSTConfiguration.setIsDebug(getApplicationContext());

        // get and set the screen dimensions
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        PSTUtilities.setScreenSize(size.x, size.y);

        Toast.makeText(getApplicationContext(), "Welcome to PeerStars Version 1.0.0.6",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Handle the Sign In button click event
     */
    public void btnSignIn_OnClick(View v) {

        TextView txtSignIn = (TextView) findViewById(R.id.txtSignIn);
        txtSignIn.setBackgroundResource(R.drawable.transbackgroundblack);
        // Start the sign in activity
        Intent intent = new Intent(this, PSTLoginActivity.class);
        startActivity(intent);
    }

    /*
     * Handle the Sign Up button click event
     */
    public void btnSignUp_OnClick(View v) {
        TextView txtSignUp = (TextView) findViewById(R.id.txtSignUp);
        txtSignUp.setBackgroundResource(R.drawable.transbackgroundblack);
        Intent intent = new Intent(this, PSTCreateAccountActivity.class);
        startActivity(intent);
    }

    /*
     * Handle the Forgot password button click
     */
    public void btnForgotPWord_OnClick(View v) {
        TextView txtForgot = (TextView) findViewById(R.id.txtForgotPWord);
        txtForgot.setBackgroundResource(R.drawable.transbackgroundblack);
        Intent intent = new Intent(this, PSTForgotPasswordActivity.class);
        startActivity(intent);
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
        //Toast.makeText(getApplicationContext(), "Load groups has failed with errors: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackResult(String result) {
        //Toast.makeText(getApplicationContext(), "Results: " + result, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackResultObject(Object result) {

    }

}
