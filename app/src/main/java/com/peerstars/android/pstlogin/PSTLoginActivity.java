package com.peerstars.android.pstlogin;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstauthentication.PSTAuthenticatedUser;
import com.peerstars.android.pstbase.PSTTabbedActivity;
import com.peerstars.android.pstconfiguration.PSTConfiguration;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.pststartup.PSTStartupActivity;
import com.peerstars.android.pststorage.PSTContentCache;
import com.peerstars.android.pststorage.PSTMembersStorage;
import com.peerstars.android.pststorage.PSTStorageHandler;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Dictionary;

public class PSTLoginActivity extends Activity implements ICallbacks {

    // Error Msg TextView Object
    TextView errorMsg;
    // Email Edit View Object
    EditText emailET;
    // Password Edit View Object
    EditText pwdET;
    // Create a user object
    String user = "";
    // Create a password object
    String passwd = "";
    // Create the process flag
    PSTProcessEnum process = PSTProcessEnum.IDLE;
    // Create an authenticated user
    PSTAuthenticatedUser authUser = new PSTAuthenticatedUser();
    // Create the token object
    String token = "";

    // create a settings manager
    SharedPreferences settings;
    SharedPreferences.Editor settingsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set the data folder
        PSTStorageHandler.setFileDir(getApplicationContext().getExternalCacheDir().getAbsolutePath());

        // create a settings manager
        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        // Find Email Edit View control by ID
        emailET = (EditText) findViewById(R.id.txtUser);
        // Find Password Edit View control by ID
        pwdET = (EditText) findViewById(R.id.txtPassword);

        try {
            // if we get a user and password, sign us in!
            Bundle b = getIntent().getExtras();
            String user = b.getString("user");
            String password = b.getString("password");
            if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
                emailET.setText(user);
                pwdET.setText(password);
                doSignIn_OnClick(new View(this));
            }
        } catch (Exception e) {
            // must not be a new user...
        }

        // cancel all pending file transfers
        //AWSTransferObserverFactory.cancelAllTransfers(getApplicationContext());

        // build the bitmap cache
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        int maxKb = am.getMemoryClass() * 1024;
        int limitKb = maxKb / 8; // 1/8th of total ram
        PSTContentCache.initializeCache(maxKb);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            // Start the sign in activity
            Intent intent = new Intent(this, PSTStartupActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Handle the sign in process.
     */
    public void doSignIn_OnClick(View v) {

        // set the button to selected
        TextView txtDoSignIn = (TextView) findViewById(R.id.txtDoSignIn);
        txtDoSignIn.setBackgroundResource(R.drawable.transbackgroundblack);

        // Get Email Edit View Value
        String email = emailET.getText().toString();

        // Get Password Edit View Value
        String password = pwdET.getText().toString();

        // build a parameters object
        Dictionary<String, String> params = PSTUtilities.getParamsObject();

        // When Email Edit View and Password Edit View have values other than Null
        if (PSTUtilities.isNotNull(email) && email.length() > 0) {
            if (PSTUtilities.isNotNull(password) && password.length() > 0) {
                // When Email entered is Valid
                if (PSTUtilities.validate(email)) {
                    // Set the process to login
                    process = PSTProcessEnum.LOGIN;
                    // Put Http parameter username with value of Email Edit View control
                    params.put("username", email);
                    settingsHandler.putString("user", email);
                    settingsHandler.commit();
                    authUser.setUser(email);
                    // Put Http parameter password with value of Password Edit Value control
                    params.put("password", password);
                    settingsHandler.putString("password", password);
                    settingsHandler.commit();
                    authUser.setPassword(password);
                    // Setup and call the Simple Request object
                    PSTClient request = new PSTClient(this, "", this);
                    request.loginUser(params.get("username"), params.get("password"));
                }
                // When Email is invalid
                else {
                    Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
                    txtDoSignIn.setBackgroundResource(0);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Blank password is not allowed", Toast.LENGTH_LONG).show();
                txtDoSignIn.setBackgroundResource(0);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Email cannot be blank", Toast.LENGTH_LONG).show();
            txtDoSignIn.setBackgroundResource(0);
        }

    }


    @Override
    public void callbackProgress(int value) {
        //Toast.makeText(getApplicationContext(), "Progress - " + value, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackComplete() {
        //if(process == PSTProcessEnum.LOGIN)
        //Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void callbackCompleteWithError(String error) {
        Toast.makeText(getApplicationContext(), "Login failed with error(s): " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackResult(String result) {

        if (result.toLowerCase().startsWith("login failed")) {
            // set the button to selected
            TextView txtDoSignIn = (TextView) findViewById(R.id.txtDoSignIn);
            txtDoSignIn.setBackgroundResource(0);
            Toast.makeText(getApplicationContext(), "Login failed - note that all fields are case sensitive.", Toast.LENGTH_LONG).show();
            return;
        }
        //Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();

        if (process == PSTProcessEnum.LOGIN) {
            // store the result
            settingsHandler.putString("token", result);
            settingsHandler.commit();
            PSTConfiguration.setToken(result);
            authUser.setToken(result);
            this.token = result;
            getUserData();
            return;
        } else if (process == PSTProcessEnum.GET_USER_DATA) {
            if (result.length() > 0) {
                setupEnvironment(result);
                process = PSTProcessEnum.GET_MEMBERS;
                getMembers();
            }
        } else if (process == PSTProcessEnum.GET_MEMBERS) {
            // store the members
            processMembers(result);

            // build and start the tabbed activity intent
            Intent intent = new Intent(this, PSTTabbedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void callbackResultObject(Object result) {

    }

    private void getUserData() {

        // set the process to GET_USER_DATE
        process = PSTProcessEnum.GET_USER_DATA;

        // Setup and call the Simple Request object
        PSTClient request = new PSTClient(this, token, this);

        // Get the user data
        request.getUserData(authUser);

    }

    private void getMembers() {

        // set the process to GET_USER_DATE
        process = PSTProcessEnum.GET_MEMBERS;

        // Setup and call the Simple Request object
        PSTClient request = new PSTClient(this, token, this);

        // get the current school year end
        Calendar today = Calendar.getInstance();
        Calendar yearEnd = Calendar.getInstance();
        yearEnd.set(today.get(Calendar.YEAR), 6, 30);
        String year = String.valueOf(today.get(Calendar.YEAR));
        if (today.after(yearEnd)) {
            year = String.valueOf(today.get(Calendar.YEAR) + 1);
        }
        String group = settings.getString("groupId", "");
        // Load the users
        request.loadMembersUrls(group, year);

    }

    // set up the environment
    private void setupEnvironment(String values) {
        try {
            // is the user data string empty?
            if (values.length() > 0) {
                // create a json object to process
                JSONObject valueset = new JSONObject(values);

                // store the user data
                storeUserValues(valueset);
            }

        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data", Toast.LENGTH_LONG);
        }
    }

    // store user values
    private void storeUserValues(JSONObject values) {
        try {
            // get the current school year end
            Calendar today = Calendar.getInstance();
            Calendar yearEnd = Calendar.getInstance();
            yearEnd.set(today.get(Calendar.YEAR), Calendar.JUNE, 30);
            String year = String.valueOf(today.get(Calendar.YEAR));
            if (today.after(yearEnd)) {
                year = String.valueOf(today.get(Calendar.YEAR) + 1);
            }
            settingsHandler.putString("id", values.getString("id"));
            settingsHandler.putString("firstName", values.getString("firstName"));
            settingsHandler.putString("lastName", values.getString("lastName"));
            settingsHandler.putString("email", values.getString("email"));
            settingsHandler.putString("gradYear", values.getString("gradYear"));
            settingsHandler.putString("yearEnd", year);
            settingsHandler.putString("photoUrl", values.getString("photoUrl"));
            settingsHandler.putBoolean("isActive", values.getBoolean("isActive"));
            settingsHandler.putString("createdAt", values.getString("createdAt"));
            settingsHandler.putString("updatedAt", values.getString("updatedAt"));
            JSONObject groupData = new JSONObject(values.getString("school"));
            settingsHandler.putString("groupId", groupData.getString("id"));
            settingsHandler.putString("govId", groupData.getString("govId"));
            settingsHandler.putString("name", groupData.getString("name"));
            settingsHandler.putString("street", groupData.getString("street"));
            settingsHandler.putString("city", groupData.getString("city"));
            settingsHandler.putString("state", groupData.getString("state"));
            settingsHandler.putString("zipCode", groupData.getString("zipCode"));
            settingsHandler.putString("schCreatedAt", groupData.getString("createdAt"));
            settingsHandler.putString("schUpdatedAt", groupData.getString("updatedAt"));

        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "JSON data storage failed with error: " + je.getLocalizedMessage(), Toast.LENGTH_LONG);
        } finally {
            settingsHandler.commit();
        }
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

            if (seniorsset.length() > 0)
                processSubGroup(seniorsset);
            if (juniorsset.length() > 0)
                processSubGroup(juniorsset);
            if (sophomoresset.length() > 0)
                processSubGroup(sophomoresset);
            if (freshmenset.length() > 0)
                processSubGroup(freshmenset);

        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data", Toast.LENGTH_LONG);
        }
    }

    private void processSubGroup(JSONArray subgroup) {

        String rc = "";
        try {
            // process the classes
            for (int i = 0; i < subgroup.length(); i++) {
                JSONObject member = subgroup.getJSONObject(i);

                PSTMembersStorage.addMember(
                        member.getInt("id"), member.getString("firstName"),
                        member.getString("lastName"),
                        String.valueOf(member.getInt("gradYear")),
                        member.getString("photoUrl"));

            }
        } catch (JSONException je) {
            //TODO: d0 something
        }
    }

    private void createItemCashe() {
        int memClass = ((ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
        LruCache cache = new LruCache<String, Bitmap>(cacheSize);
    }
}