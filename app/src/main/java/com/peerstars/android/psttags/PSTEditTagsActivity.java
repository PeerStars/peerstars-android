
package com.peerstars.android.psttags;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pststorage.PSTTagsStorage;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class PSTEditTagsActivity extends AppCompatActivity implements ICallbacks {

    // create the shared preferences accessor
    public SharedPreferences settings;
    public SharedPreferences.Editor settingsHandler;

    private String groupId = "";
    private String token = "";

    private ArrayList tagsArray = new ArrayList();

    // create the GridView for the photos
    ListView listView;

    // create an output string array
    String[] outputStrArr;

    // create a current id object
    String id = "0";

    // create the progress bar
    ProgressBar progressBar;

    // get the button layout
    RelativeLayout btnLayout;

    // Hold the tags names and ids
    HashMap<String, Integer> idsmap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tags);

        // create the shared preferences accessor
        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        // Set the title to Varsity font
        setTitle(PSTUtilities.getCustomFont("Edit Tags", getApplicationContext()));

        // Get the progress bar
        progressBar = (ProgressBar) findViewById(R.id.tagsProgressBar);

        // get the button layout
        btnLayout = (RelativeLayout) findViewById(R.id.btnLayout);

        // Initialize the image view
        listView = (ListView) findViewById(R.id.gridTags);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, tagsArray);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                ArrayList<String> selectedItems = new ArrayList<String>();
                for (int i = 0; i < checked.size(); i++) {
                    // Item position in adapter
                    position = checked.keyAt(i);
                    // Add tag if it is checked i.e.) == TRUE!
                    if (checked.valueAt(i))
                        selectedItems.add(adapter.getItem(position));
                }

                outputStrArr = new String[selectedItems.size()];

                for (int i = 0; i < selectedItems.size(); i++) {
                    outputStrArr[i] = selectedItems.get(i);
                }

                // Create a bundle object
                Bundle b = new Bundle();
                b.putStringArray("selectedItems", outputStrArr);
            }
        });

        // Load the tags
        token = settings.getString("token", "");
        groupId = settings.getString("groupId", "");

        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        btnLayout.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        requestTags(token, id);

    }

    /*
     * load the private feed
     */
    public void requestTags(String token, String groupId) {

        // Setup and call the Simple Request object
        PSTClient request = new PSTClient(this, token, this);
        if (PSTTagsStorage.getTagsJson().length() < 1) {
            request.loadTags(token, groupId);
        } else {
            loadTheTags(PSTTagsStorage.getTagsJson());
        }

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
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        //Toast.makeText(getApplicationContext(), "Results: " + result, Toast.LENGTH_LONG).show();
        loadTheTags(result);
    }

    @Override
    public void callbackResultObject(Object result) {
        // Toast.makeText(getApplicationContext(), "Get user data has returned an object: " + result.toString(), Toast.LENGTH_LONG);
    }

    // load the feed
    private void loadTheTags(String values) {

        // save the json string
        PSTTagsStorage.setTagsJson(values);
        String[] tags;
        String value;
        HashMap tagsMap = new HashMap();
        tagsMap.put("nothing", "nothing");
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            tags = bundle.getString("tags").split(" ");
            for (String tag : tags) {
                tagsMap.put(tag, tag);
            }
        }

        String rc = "";
        ArrayAdapter<String> adaptor = (ArrayAdapter<String>) listView.getAdapter();
        try {
            JSONArray tagsSet = new JSONArray(values);
            if (tagsSet.length() > 0)
                tagsArray.clear();
            for (int i = 0; i < tagsSet.length(); i++) {
                value = "#" + tagsSet.getJSONObject(i).getString("name");
                int tagId = tagsSet.getJSONObject(i).getInt("id");
                idsmap.put(value, tagId);
                if (tagsMap.containsKey(value)) {
                    tagsArray.add(value);
                    listView.setItemChecked(i, true);
                } else {
                    adaptor.add(value);
                }
            }

            // reload the grid view
            listView.invalidate();

        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data: ", Toast.LENGTH_LONG);
        }

        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.VISIBLE);

    }

    public void onClick_Save(View v) {
        String tags = "";
        ArrayList<String> ids = new ArrayList<>();
        Intent returnIntent = getIntent();
        for (String item : outputStrArr) {
            tags += item + " ";
            ids.add(String.valueOf(idsmap.get(item)));
        }
        Bundle bundle = getIntent().getExtras();
        returnIntent.putExtra("result", tags);
        returnIntent.putExtra("id", bundle.getString("id"));
        returnIntent.putExtra("tagIds", ids.toString());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void onClick_Cancel(View v) {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
        }
        return super.onKeyDown(keyCode, event);
    }

}
