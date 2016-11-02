package com.peerstars.android.pstprofile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.peerstars.android.R;
import com.peerstars.android.pstconfiguration.PSTConfiguration;
import com.peerstars.android.pstfeed.PSTCustomFeedAdapter;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.pstpeerbook_webpage.PeerStarsWebPageActivity;
import com.peerstars.android.pststorage.PSTContentCache;
import com.peerstars.android.pststorage.PSTPrivateFeedStorage;
import com.peerstars.android.pstutilities.CroppingOption;
import com.peerstars.android.pstutilities.CroppingOptionAdapter;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PSTProfileActivity extends AppCompatActivity implements ICallbacks {

    // create the video objects
    ProgressDialog progressDialog;
    private MediaController mediaControls;

    // create all necessary objects
    String token = "";
    String photoUrl = "";
    ImageView profileImage;
    TextView name;

    String groupId = "";
    String memberId = "";
    String filters = "";
    String year = "";

    // create the GridView for the photos
    GridView gridView;

    Boolean isImageComplete = false;

    // setup for cropping image
    static final int CAMERA_CODE = 101, GALLERY_CODE = 201, CROPPING_CODE = 301;
    Uri mImageCaptureUri;
    File outPutFile = null;
    Bitmap photo;

    // create a new file hash object
    BigInteger hashCode;
    String sHashCode;
    String sUuid;

    // create the file url
    String fileKey;

    // Create the process flag
    PSTProcessEnum process = PSTProcessEnum.IDLE;

    // create a settings manager
    SharedPreferences settings;
    SharedPreferences.Editor settingsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        // load the token
        token = settings.getString("token", "");

        // load the photo url
        photoUrl = settings.getString("photoUrl", "");

        // get the widgets
        profileImage = (ImageView) findViewById(R.id.profilePhoto);
        name = (TextView) findViewById(R.id.spanMyPeerBook);

        // Initialize the image view
        gridView = (GridView) findViewById(R.id.gridViewProfile);
        gridView.setAdapter(new PSTCustomFeedAdapter(this));

        name.setText(PSTUtilities.getCustomFont("My Peerbook", getApplicationContext()));

        // set the title
        setTitle(PSTUtilities.getCustomFont(settings.getString("firstName", "") + " " + settings.getString("lastName", ""), this));

        // set the media controls
        if (mediaControls == null) {
            mediaControls = new MediaController(this);
        }

        // create the progress bar while the video file loads
        progressDialog = new ProgressDialog(this);
        // set a title for the progress bar
        progressDialog.setTitle("Video File");
        // set the message for the progress bar
        progressDialog.setMessage("Loading...");
        // set the progress bar to "Not Cancelable"
        progressDialog.setCancelable(false);

        // load the profile picture
        PSTClient request = new PSTClient(this, token, this);
        process = PSTProcessEnum.GET_IMAGE;
        request.getImageFromUrl(PSTConfiguration.getCurrentBucket(), photoUrl);

        // setup for cropping the user image
        outPutFile = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
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
        if (process == PSTProcessEnum.GET_IMAGE) {
            //Toast.makeText(getApplicationContext(), "Results: " + result, Toast.LENGTH_LONG).show();
        }
        if (process == PSTProcessEnum.GET_PROFILE_ITEMS) {
            loadTheFeed(result);
        }
    }

    @Override
    public void callbackResultObject(Object result) {
        if (process == PSTProcessEnum.GET_IMAGE) {
            // Toast.makeText(getApplicationContext(), "Get user data has returned an object: " + result.toString(), Toast.LENGTH_LONG);
            if (result instanceof Bitmap) {
                Bitmap bm = (Bitmap) result;
                profileImage.setImageBitmap(bm);
                int sides = (int) (PSTUtilities.getScrnWidth() * .4);
                profileImage.getLayoutParams().height = sides;
                profileImage.getLayoutParams().width = sides;
                profileImage.requestLayout();
            }

            // Setup and call the Simple Request object
            PSTClient request = new PSTClient(this, token, this);

            // Show Progress Dialog
            //prgDialog.show();

            if (PSTPrivateFeedStorage.getPrivateFeedJson().length() > 1) {
                loadTheFeed(PSTPrivateFeedStorage.getPrivateFeedJson());
                return;
            }

            // Load the feed
            year = settings.getString("yearEnd", "");
            groupId = settings.getString("groupId", "");
            memberId = settings.getString("id", "");
            token = settings.getString("token", "");

            requestFeedItems(year, groupId, memberId, token);

        }
    }

    public void requestFeedItems(String year, String groupId, String memberId, String token) {

        // Setup and call the Simple Request object
        PSTClient request = new PSTClient(this, token, this);

        filters = "memberId:" + memberId;
        process = PSTProcessEnum.GET_PROFILE_ITEMS;
        request.loadFeedUrls(groupId, year, filters, "25");
    }

    // load the feed
    private void loadTheFeed(String values) {

        PSTCustomFeedAdapter adaptor = (PSTCustomFeedAdapter) gridView.getAdapter();
        try {
            JSONArray mediaset = new JSONArray(values);
            if (mediaset.length() > 0)
                adaptor.clearItems();
            for (int i = 0; i < mediaset.length(); i++)
                processMediaItemJSONObject(mediaset.getJSONObject(i), adaptor);
        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data: ", Toast.LENGTH_LONG).show();
        }
    }

    private void processMediaItemJSONObject(JSONObject mediaObject, PSTCustomFeedAdapter adaptor) {

        String id = "";
        String rc = "";
        StringBuilder tagBuilder = new StringBuilder();
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
        String flagged = "";

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
                rc = adaptor.addItem(params, token);
                adaptor.notifyDataSetInvalidated();
                gridView.invalidate();
            }
            if (rc.toLowerCase().equals("failed")) {
                // TODO: something
            }
        } catch (JSONException je) {
            Toast.makeText(getApplicationContext(), "Invalid JSON data: " + je.getLocalizedMessage(), Toast.LENGTH_LONG);
        }

    }

    public void retakeProfilePicture(View v) {
        final
        CharSequence[] items = {"Take My Picture", "Choose a Picture from the Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take My Picture")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp1.jpg");
                    mImageCaptureUri = Uri.fromFile(f);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    startActivityForResult(intent, CAMERA_CODE);

                } else if (items[item].equals("Choose a Picture from the Gallery")) {

                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, GALLERY_CODE);

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {
            mImageCaptureUri = data.getData();
            System.out.println("Gallery Image URI : " + mImageCaptureUri);
            CroppingIMG();
        } else if (requestCode == CAMERA_CODE && resultCode == Activity.RESULT_OK) {
            System.out.println("Camera Image URI : " + mImageCaptureUri);
            CroppingIMG();
        } else if (requestCode == CROPPING_CODE) {

            try {
                if (outPutFile.exists()) {
                    photo = decodeFile(outPutFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, baos); //photo is the bitmap object
                    byte[] bitmapBytes = baos.toByteArray();
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    md5.update(bitmapBytes, 0, bitmapBytes.length);
                    hashCode = new BigInteger(1, md5.digest());
                    sHashCode = String.valueOf(Math.abs(hashCode.longValue()));
                    sUuid = UUID.randomUUID().toString();
                    profileImage.setImageBitmap(photo);
                    profileImage.setBackgroundResource(R.drawable.transbackgroundwhitegreenborder);
                    isImageComplete = true;


                    // Setup and call the Simple Request object
                    PSTClient request = new PSTClient(this, token, this);

                    // Set the process
                    process = PSTProcessEnum.PUT_PHOTO;

                    // create a key string
                    fileKey = photoUrl;
                    request.uploadImage(fileKey, outPutFile.getAbsolutePath());

                    // write it to the cache
                    PSTContentCache.clearImageCashe();
                    PSTContentCache.addBitmapToMemoryCache(photoUrl, photo);

                } else {
                    Toast.makeText(getApplicationContext(), "Error while saving image", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void CroppingIMG() {

        final ArrayList croppingOptions = new ArrayList();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List list = getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Can't find image cropping app", Toast.LENGTH_SHORT).show();
        } else {
            intent.setData(mImageCaptureUri);
            intent.putExtra("outputX", 512);
            intent.putExtra("outputY", 512);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);

            //TODO: don't use return-data tag because it's not return large image data and crash not given any message
            //intent.putExtra("return-data", true);

            //Create output file here
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outPutFile));

            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = (ResolveInfo) list.get(0);

                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                startActivityForResult(i, CROPPING_CODE);
            } else {
                for (Object r : list) {
                    ResolveInfo res = (ResolveInfo) r;
                    final CroppingOption co = new CroppingOption();

                    co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);
                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    croppingOptions.add(co);
                }

                CroppingOptionAdapter adapter = new CroppingOptionAdapter(getApplicationContext(), croppingOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Cropping App");
                builder.setCancelable(false);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        startActivityForResult(((CroppingOption) croppingOptions.get(item)).appIntent, CROPPING_CODE);
                    }
                });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (mImageCaptureUri != null) {
                            getContentResolver().delete(mImageCaptureUri, null, null);
                            mImageCaptureUri = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 512;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public void onClick_PeerBook(View v) {
        Intent intent = new Intent(this, PeerStarsWebPageActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
        }
        return super.onKeyDown(keyCode, event);
    }
}
