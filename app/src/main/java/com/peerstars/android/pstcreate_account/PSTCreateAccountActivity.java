package com.peerstars.android.pstcreate_account;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.peerstars.android.R;
import com.peerstars.android.pstlogin.PSTLoginActivity;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.pstutilities.CroppingOption;
import com.peerstars.android.pstutilities.CroppingOptionAdapter;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PSTCreateAccountActivity extends Activity implements ICallbacks {

    private ImageView imgView;
    private TextView txtFName;
    private TextView txtLName;
    private TextView txtEmail;
    private TextView txtPassword;
    private TextView txtYear;
    private String groupName;
    private String groupId;

    private Boolean isImageComplete = false;
    private Boolean isFNameComplete = false;
    private Boolean isLNameComplete = false;
    private Boolean isEMailComplete = false;
    private Boolean isPWordComplete = false;
    private Boolean isYearComplete = false;

    // setup for cropping image
    private static final int CAMERA_CODE = 101, GALLERY_CODE = 201, CROPPING_CODE = 301, SELECT_GROUP_CODE = 401;
    private Uri mImageCaptureUri;
    private File outPutFile = null;
    private Bitmap photo;

    // create a new file hash object
    BigInteger hashCode;
    String sHashCode;
    String sUuid;

    // Create the process flag
    PSTProcessEnum process = PSTProcessEnum.IDLE;

    // create a settings manager
    SharedPreferences settings;
    SharedPreferences.Editor settingsHandler;

    // create the file url
    String fileKey;

    // create the metadata for the create operation
    ObjectMetadata metadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // create a settings manager
        settings = getSharedPreferences("PeerStarsSettings", 0);
        settingsHandler = settings.edit();

        setTitle(PSTUtilities.getCustomFont("Create Account", getApplicationContext()));

        // setup the user imageview for changes
        imgView = (ImageView) findViewById(R.id.imgUser);

        // setup the text fields for changes
        txtFName = (TextView) findViewById(R.id.txtFirstName);
        txtFName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (txtFName.getText().length() > 0) {
                    txtFName.setBackgroundResource(R.drawable.transbackgroundwhitegreenborder);
                    isFNameComplete = true;
                } else {
                    txtFName.setBackgroundResource(R.drawable.transbackgroundwhiteredborder);
                    isFNameComplete = false;
                }
            }
        });

        txtLName = (TextView) findViewById(R.id.txtLastName);
        txtLName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (txtLName.getText().length() > 0) {
                    txtLName.setBackgroundResource(R.drawable.transbackgroundwhitegreenborder);
                    isLNameComplete = true;
                } else {
                    txtLName.setBackgroundResource(R.drawable.transbackgroundwhiteredborder);
                    isLNameComplete = false;
                }
            }
        });
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtEmail.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                txtEmail.setBackgroundResource(R.drawable.transbackgroundwhiteredborder);
                isEMailComplete = false;
                if (txtEmail.getText().toString().contains("@")) {
                    int atindex = txtEmail.getText().toString().indexOf("@");
                    int dotindex = txtEmail.getText().toString().indexOf(".");
                    int emailLen = txtEmail.getText().length();
                    if (dotindex > atindex && (emailLen - dotindex) > 1) {
                        txtEmail.setBackgroundResource(R.drawable.transbackgroundwhitegreenborder);
                        isEMailComplete = true;
                    }
                }
            }
        });
        txtPassword = (TextView) findViewById(R.id.txtPassword);
        txtPassword.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (txtPassword.getText().length() > 3) {
                    txtPassword.setBackgroundResource(R.drawable.transbackgroundwhitegreenborder);
                    isPWordComplete = true;
                } else {
                    txtPassword.setBackgroundResource(R.drawable.transbackgroundwhiteredborder);
                    isPWordComplete = false;
                }
            }
        });
        txtYear = (TextView) findViewById(R.id.txtGradYear);
        txtYear.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (txtYear.getText().length() < 1)
                    return;
                Calendar cal = Calendar.getInstance();
                if (Integer.parseInt(txtYear.getText().toString()) >= cal.get(Calendar.YEAR)) {
                    txtYear.setBackgroundResource(R.drawable.transbackgroundwhitegreenborder);
                    isYearComplete = true;
                } else {
                    txtYear.setBackgroundResource(R.drawable.transbackgroundwhiteredborder);
                    isYearComplete = false;
                }
            }
        });

        // setup for cropping the user image
        outPutFile = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_account, menu);
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

    public void takeProfilePicture(View v) {
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
        } else if (requestCode == SELECT_GROUP_CODE && resultCode == Activity.RESULT_OK) {
            String[] results = data.getDataString().split(",");
            groupId = results[0];
            groupName = results[1];

            // create a new request client
            final PSTClient request = new PSTClient(this, "", this);

            // custom dialog
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.accept_create_account);
            dialog.setTitle(PSTUtilities.getCustomFont("Create Account?", getApplicationContext()));

            // set the custom dialog components - text, image and button
            TextView dlgGroup = (TextView) dialog.findViewById(R.id.dlgGroup);
            dlgGroup.setText(groupName);
            TextView dlgName = (TextView) dialog.findViewById(R.id.dlgName);
            dlgName.setText(txtFName.getText() + " " + txtLName.getText());
            TextView dlgEMail = (TextView) dialog.findViewById(R.id.dlgEmail);
            dlgEMail.setText(txtEmail.getText());
            TextView dlgYear = (TextView) dialog.findViewById(R.id.dlgYear);
            dlgYear.setText(txtYear.getText());

            Button acceptButton = (Button) dialog.findViewById(R.id.btnAccept);
            Button cancelButton = (Button) dialog.findViewById(R.id.btnCancel);
            // if button is clicked, close the custom dialog
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    JSONObject userdata = null;

                    dialog.dismiss();

                    // create a key string
                    fileKey = "content/school-" + groupId + "/profile-photo-" + sHashCode + "/" + sUuid + ".jpg";
                    metadata = new ObjectMetadata();

                    Map<String, String> data = new HashMap<String, String>();
                    data.put("firstName", txtFName.getText().toString());
                    data.put("lastName", txtLName.getText().toString());
                    data.put("email", txtEmail.getText().toString());
                    data.put("password", txtPassword.getText().toString());
                    data.put("photoUrl", fileKey);
                    data.put("gradYear", txtYear.getText().toString());

                    try {
                        userdata = PSTUtilities.getJsonObjectFromMap(data);
                    } catch (JSONException je) {
                        Log.d("Create User failed!", je.getLocalizedMessage());
                    }
                    metadata.setUserMetadata(data);

                    // Set the process to create account
                    process = PSTProcessEnum.CREATE_ACCOUNT;

                    // Store the user data elements locally
                    settingsHandler.putString("groupId", groupId);
                    settingsHandler.putString("firstName", txtFName.getText().toString());
                    settingsHandler.putString("lastName", txtLName.getText().toString());
                    settingsHandler.putString("email", txtEmail.getText().toString());
                    settingsHandler.putString("password", txtPassword.getText().toString());
                    settingsHandler.putString("photoUrl", fileKey);
                    settingsHandler.putString("gradYear", txtYear.getText().toString());

                    // Setup and call the Simple Request object
                    request.createAccount(groupId, userdata.toString());

                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();

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
                    imgView.setImageBitmap(photo);
                    imgView.setBackgroundResource(R.drawable.transbackgroundwhitegreenborder);
                    isImageComplete = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Error while save image", Toast.LENGTH_SHORT).show();
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
            return;
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

    /*
     * Handle the Select Group button click
     */
    public void btnSelectGroup_OnClick(View v) {
        if (isImageComplete && isFNameComplete && isLNameComplete && isEMailComplete && isPWordComplete && isYearComplete) {
            TextView txtSelectGroup = (TextView) findViewById(R.id.txtSelectGroup);
            txtSelectGroup.setBackgroundResource(R.drawable.transbackgroundblack);
            // start the select group activity
            Intent intent = new Intent(this, PSTSelectGroupActivity.class);
            startActivityForResult(intent, SELECT_GROUP_CODE);
        } else {
            if (!isImageComplete)
                Toast.makeText(getApplicationContext(), "Please set your profile picture!", Toast.LENGTH_LONG).show();
            if (!isFNameComplete)
                Toast.makeText(getApplicationContext(), "Please check and complete your first name!", Toast.LENGTH_LONG).show();
            if (!isLNameComplete)
                Toast.makeText(getApplicationContext(), "Please check and complete your last name!", Toast.LENGTH_LONG).show();
            if (!isEMailComplete)
                Toast.makeText(getApplicationContext(), "Please check and complete your email address!", Toast.LENGTH_LONG).show();
            if (!isPWordComplete)
                Toast.makeText(getApplicationContext(), "Please check and complete your password!", Toast.LENGTH_LONG).show();
            if (!isYearComplete)
                Toast.makeText(getApplicationContext(), "Please check and complete your graduation year!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Handle the Cancel button click
     */
    public void btnCancel_OnClick(View v) {
        TextView txtCancel = (TextView) findViewById(R.id.txtCancel);
        txtCancel.setBackgroundResource(R.drawable.transbackgroundblack);
        finish();
    }


    @Override
    public void callbackProgress(int value) {
        Toast.makeText(getApplicationContext(), "Progress - " + value, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackComplete() {
        if (process == PSTProcessEnum.CREATE_ACCOUNT)
            Toast.makeText(getApplicationContext(), "User Created...", Toast.LENGTH_SHORT).show();
        if (process == PSTProcessEnum.PUT_PHOTO) {
            Toast.makeText(getApplicationContext(), "Photo Uploaded...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PSTLoginActivity.class);

            intent.putExtra("user", txtEmail.getText().toString());
            intent.putExtra("password", txtPassword.getText().toString());
            startActivity(intent);
        }
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

        if (process == PSTProcessEnum.CREATE_ACCOUNT) {

            // create a new request client
            process = PSTProcessEnum.PUT_PHOTO;
            final PSTClient request = new PSTClient(this, "", this);
            request.uploadImage(fileKey, outPutFile.getAbsolutePath());

        }

        if (process == PSTProcessEnum.PUT_PHOTO) {
            Toast.makeText(this, "Image added for new user.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void callbackResultObject(Object result) {

    }

}
