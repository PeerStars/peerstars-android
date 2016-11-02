package com.peerstars.android.pstcamera;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.util.IOUtils;
import com.peerstars.android.R;
import com.peerstars.android.pstbase.PSTTabbedActivity;
import com.peerstars.android.pstnetwork.PSTClient;
import com.peerstars.android.pstnetwork.PSTProcessEnum;
import com.peerstars.android.pststorage.PSTPrivateFeedStorage;
import com.peerstars.android.pstutilities.ICallbacks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PSTCameraActivity extends Activity implements PictureCallback,
        SurfaceHolder.Callback, ICallbacks {

    private static final String TAG = "PSTCameraActivity";
    private static int RESULT_LOAD_IMAGE = 1;
    private static int SHOW_IMAGE = 2;
    private static int SHOW_VIDEO = 3;

    // photo/video flag
    int capturedType = 0; // 0 = photo

    // create a settings manager
    SharedPreferences settings;
    SharedPreferences.Editor settingsHandler;

    Boolean isVideo;
    Boolean isRecording = false;
    Camera camera;
    MediaRecorder recorder;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    int currentCameraId = 0;

    Camera.PictureCallback jpegCallback;
    CountDownTimer recordTimer;

    Button captureButton;
    Button photoButton;
    Button videoButton;
    ImageView cameraRollButton;
    TextView landscapeOnly;
    TextView counter;

    Bitmap lastImage;
    String lastVideoPath;
    Bitmap rotatedImage;

    // All content objects
    String tags;
    int secret = 0;

    // Create the user data objects
    String groupId;
    String memberId;
    String year;
    String token;

    // Create the process flag
    PSTProcessEnum process = PSTProcessEnum.IDLE;

    // create a new file hash object
    BigInteger hashCode;
    String sHashCode;
    String sUuid;

    // create the file url
    String fileKey;
    String filePath;

    // Create a circular progress indicator.
    ProgressBar progressBar;
    TextView progressTxt;

    // create an entries json object
    JSONObject entriesJson;

    // Capture the selected tag ids
    String tagIds = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        try {
            // create a settings manager
            settings = getSharedPreferences("PeerStarsSettings", 0);
            settingsHandler = settings.edit();

            PSTTabbedActivity.hideTabs();

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

            surfaceView = (SurfaceView) findViewById(R.id.surfaceViewCamera);
            surfaceHolder = surfaceView.getHolder();

            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            captureButton = (Button) findViewById(R.id.camera_capture_button);
            cameraRollButton = (ImageView) findViewById(R.id.button_camera_roll);

            photoButton = (Button) findViewById(R.id.button_photo);
            videoButton = (Button) findViewById(R.id.button_video);
            photoButton.setBackgroundResource(R.drawable.transbackgroundblack);

            counter = (TextView) findViewById(R.id.txtCounter);
            counter.setVisibility(View.INVISIBLE);

            groupId = settings.getString("groupId", "");
            memberId = settings.getString("id", "");
            year = settings.getString("gradYear", "");
            token = settings.getString("token", "");

            try {
                isVideo = settings.getBoolean("isVideo", false);
            } catch (Exception e) {
                isVideo = false;
                settingsHandler.putBoolean("isVideo", isVideo);
                settingsHandler.commit();
            }
            if (isVideo) {
                photoButton.setBackgroundResource(R.drawable.transbackgroundgrey);
                videoButton.setBackgroundResource(R.drawable.transbackgroundblack);
                photoButton.setTextColor(Color.GRAY);
                videoButton.setTextColor(Color.WHITE);
            } else {
                photoButton.setBackgroundResource(R.drawable.transbackgroundblack);
                videoButton.setBackgroundResource(R.drawable.transbackgroundgrey);
                photoButton.setTextColor(Color.WHITE);
                videoButton.setTextColor(Color.GRAY);
            }

            landscapeOnly = (TextView) findViewById(R.id.textLandscapeOnly);

            try {
                Bitmap bm = BitmapFactory.decodeFile(settings.getString("last_image", ""));
                cameraRollButton.setImageBitmap(bm);
            } catch (Exception e) {
            }

            // Fires when picture is taken to save the image
            jpegCallback = new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try {

                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        //options.inSampleSize = 10; // Down sample 10x

                        // create a bitmap from the data array
                        lastImage = BitmapFactory.decodeStream(bis, null, options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    refreshCamera();

                    try {

                        // create a matrix object to rotate the image.
                        Matrix matrix = new Matrix();

                        String orientation = getRotation(getApplicationContext());
                        if (orientation.equals("portrait"))
                            if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                matrix.postRotate(90); //90
                            } else {
                                matrix.postRotate(270);
                            }
                        if (orientation.equals("landscape"))
                            matrix.postRotate(0);
                        if (orientation.equals("reverse portrait"))
                            matrix.postRotate(270); //270
                        if (orientation.equals("reverse landscape"))
                            matrix.postRotate(180); //180

                        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(lastImage,400,400,true);
                        rotatedImage = Bitmap.createBitmap(lastImage, 0, 0, lastImage.getWidth(), lastImage.getHeight(), matrix, true);

                        cameraRollButton.setImageBitmap(rotatedImage);

                        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
                        String url = insertImage(getApplicationContext().getContentResolver(), rotatedImage, String.format(Locale.US, "PIC_%d",
                                System.currentTimeMillis()), String.format("New picture on %s", today));
                        String path = getRealPathFromURI(Uri.parse(url));
                        settingsHandler.putString("last_image", path);
                        settingsHandler.commit();

                        Intent iview = new Intent(getApplicationContext(), PSTViewContentActivity.class);
                        iview.putExtra("type", "image");
                        iview.putExtra("path", path);
                        startActivityForResult(iview, SHOW_IMAGE);
                        capturedType = 0;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Camera Error: " + ex.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "Camera Stacktrace: " + ex.getStackTrace(),
                    Toast.LENGTH_LONG).show();
        }

    }

    /*
     * Handle the capture button click event
     */
    public void btnCapture_OnClick(View v) {

        try {
            if (isVideo) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            } else {
                // Capture photo
                camera.takePicture(null, null, jpegCallback);
            }
        } catch (Exception e) {
            // do something!!!
            camera.release();
            camera = null;
            //recorder.stop();
            recorder = null;
            e.printStackTrace();
        }
    }

    /*
     * Handle the photo button click event
     */
    public void btnPhoto_OnClick(View v) {

        try {
            // Start the sign in activity
            isVideo = false;
            counter.setVisibility(View.INVISIBLE);
            settingsHandler.putBoolean("isVideo", isVideo);
            settingsHandler.commit();
            photoButton.setBackgroundResource(R.drawable.transbackgroundblack);
            videoButton.setBackgroundResource(R.drawable.transbackgroundgrey);
            photoButton.setTextColor(Color.WHITE);
            videoButton.setTextColor(Color.GRAY);
            checkOrientation();
        } catch (Exception e) {
            // do something!!!
        }

    }

    /*
     * Handle the video button click event
     */
    public void btnVideo_OnClick(View v) {

        try {
            // Start the sign in activity
            isVideo = true;
            settingsHandler.putBoolean("isVideo", isVideo);
            settingsHandler.commit();
            videoButton.setBackgroundResource(R.drawable.transbackgroundblack);
            photoButton.setBackgroundResource(R.drawable.transbackgroundgrey);
            photoButton.setTextColor(Color.GRAY);
            videoButton.setTextColor(Color.WHITE);
            checkOrientation();
        } catch (Exception e) {
            // do something!!!
        }

    }

    /*
    * Handle the gallery button click event
    */
    public void btnGallery_OnClick(View v) {
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    /*
     * Handle the Back button click event
     */
    public void btnBack_OnClick(View v) {

        PSTTabbedActivity.showTabs();
        PSTTabbedActivity.gotoFirstTab();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    public void refreshCamera() {
        Toast.makeText(this, "Test 1", Toast.LENGTH_SHORT);
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
            Toast.makeText(this, "Camera.stopPreview failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Toast.makeText(this, "Camera.setPreview failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
        }
    }

    public void btnSwitchCameras_OnClick(View v) {

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }

        camera.release();

        //swap the id of the camera to be used
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        camera = Camera.open(currentCameraId);

        checkOrientation();

        refreshCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // Set the gallery button background to the selected picture
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            settingsHandler.putString("last_image", picturePath);
            settingsHandler.commit();
            cameraRollButton.setImageBitmap(bitmap);
        }

        // If this was a content viewing do one of the following.
        if (requestCode == SHOW_IMAGE && resultCode == RESULT_OK) {

            capturedType = 0;
            tags = data.getStringExtra("tags");
            tagIds = data.getStringExtra("tagIds");
            secret = data.getIntExtra("secret", 0);
            filePath = data.getStringExtra("path");
            createFileEntry();
        }
        if (requestCode == SHOW_VIDEO && resultCode == RESULT_OK) {
            capturedType = 1;
            tags = data.getStringExtra("tags");
            tagIds = data.getStringExtra("tagIds");
            secret = data.getIntExtra("secret", 0);
            filePath = data.getStringExtra("path");
            createFileEntry();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open(currentCameraId);
        } catch (RuntimeException e) {
            Log.e("Camera.open failed", e.getLocalizedMessage());
            Toast.makeText(this, "Camera.open failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
            return;
        }

        checkOrientation();

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e("setPreview failed", e.getLocalizedMessage());
            Toast.makeText(this, "Camera.setPreviewDisplay failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        refreshCamera();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            camera.stopPreview();
            camera.release();
            camera = null;
        } catch (Exception e) {
            Log.e("stopPreview failed", e.getLocalizedMessage());
            Toast.makeText(this, "SurfaceDestroyed failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // TODO Auto-generated method stub

    }

    public void startRecording() throws Exception {
        if (camera == null) {
            Camera.open(currentCameraId);
        }
        camera.unlock();
        if (recorder == null) recorder = new MediaRecorder();

        // add an error listener to the recorder object
        recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.i(TAG, "Error");
            }
        });

        recorder.setCamera(camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CamcorderProfile cpQuality = CamcorderProfile
                .get(CamcorderProfile.QUALITY_480P);
        recorder.setProfile(cpQuality);
        recorder.setVideoFrameRate(24);
        recorder.setMaxDuration(30000); // 30 seconds
        recorder.setPreviewDisplay(surfaceHolder.getSurface());
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                if (i == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording();
                }
            }
        });


        try {
            lastVideoPath = getOutputMediaFile(isVideo).getAbsolutePath();
        } catch (Exception e) {
            lastVideoPath = "/sdcard/VID_Unknown.mp4";
        }
        recorder.setOutputFile(lastVideoPath);
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            throw (e);
        } catch (Exception e) {
            // Catch all other exceptions
            throw (e);
        }
        recordTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                counter.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                counter.setText("");
            }
        }.start();
        recorder.start();
        counter.setVisibility(View.VISIBLE);
        isRecording = true;
        captureButton.setBackgroundResource(R.drawable.camera_capture_button_red68);
    }

    public void stopRecording() {
        recorder.stop();
        recorder.release();
        isRecording = false;
        recordTimer.cancel();
        counter.setText("");
        counter.setVisibility(View.INVISIBLE);
        Bitmap thumb = createThumbnailAtTime(lastVideoPath, 1);
        settingsHandler.putString("last_image", lastVideoPath);
        settingsHandler.commit();
        cameraRollButton.setImageBitmap(thumb);
        captureButton.setBackgroundResource(R.drawable.camera_capture_button68);
        recorder = null;
        MediaScannerConnection.scanFile(this, new String[]{lastVideoPath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

        Intent iview = new Intent(getApplicationContext(), PSTViewContentActivity.class);
        iview.putExtra("type", "video");
        iview.putExtra("path", lastVideoPath);
        startActivityForResult(iview, SHOW_VIDEO);
        capturedType = 1;
    }

    /**
     * API for generating Thumbnail from particular time frame
     *
     * @param filePath      - video file path
     * @param timeInSeconds - thumbnail to generate at time
     * @return- thumbnail bitmap
     */
    private Bitmap createThumbnailAtTime(String filePath, int timeInSeconds) {
        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        mMMR.setDataSource(filePath);
        //api time unit is microseconds
        return mMMR.getFrameAtTime(timeInSeconds * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
    }

    /*
     * Find the pictures directory and put the file there
     */
    public File getOutputMediaFile(boolean isVideo) {
        if (!isVideo) {
            // Get the pictures directory and file
            File mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "PeerStars");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("PeerStars", "failed to create directory");
                    return null;
                }
            }
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            return new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            // Get the video directory and file
            File mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "PeerStars");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("PeerStars", "failed to create directory");
                    return null;
                }
            }
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            return new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        }

    }

    /*
     * get the last image id
     */
    @SuppressWarnings("unused")
    private String getLastImageId() {
        final String[] imageColumns = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA};
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor imageCursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
                null, null, imageOrderBy);
        if (imageCursor.moveToFirst()) {
            int id = imageCursor.getInt(imageCursor
                    .getColumnIndex(MediaStore.Images.Media._ID));
            String fullPath = imageCursor.getString(imageCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));

            imageCursor.close();
            return fullPath;
        } else {
            return "no path";
        }
    }

    /*
     * Returns the orientation of the actual phone
     */
    public String getRotation(Context context) {
        final int rotation = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return "portrait";
            case Surface.ROTATION_90:
                return "landscape";
            case Surface.ROTATION_180:
                return "reverse portrait";
            default:
                return "reverse landscape";
        }
    }

    private void checkOrientation() {
        captureButton.setEnabled(true);
        Camera.Parameters param;
        param = camera.getParameters();
        String orientation = getRotation(this.getApplicationContext());
        if (orientation.toLowerCase().equals("portrait")) {
            param.set("orientation", "portrait");
            param.set("rotation", 90);
            camera.setDisplayOrientation(90);
            if (isVideo) {
                landscapeOnly.setVisibility(View.VISIBLE);
                captureButton.setEnabled(false);
            } else {
                landscapeOnly.setVisibility(View.INVISIBLE);
            }
        }
        if (orientation.toLowerCase().equals("landscape")) {
            param.set("orientation", "landscape");
            param.set("rotation", 0);
            landscapeOnly.setVisibility(View.INVISIBLE);
        }
        if (orientation.toLowerCase().equals("reverse landscape")) {
            //param.set("orientation", "landscape");
            //param.set("rotation", 180);
            //camera.setDisplayOrientation(180);
            //landscapeOnly.setVisibility(View.INVISIBLE);
            if (isVideo) {
                landscapeOnly.setVisibility(View.VISIBLE);
                captureButton.setEnabled(false);
            } else {
                landscapeOnly.setVisibility(View.INVISIBLE);
            }
        }
        // param.setPreviewSize(352, 288);
        camera.setParameters(param);
    }

    /*
     * Get the size of the bitmap in bytes
     */
    protected int byteSizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return data.getByteCount();
        } else {
            return data.getAllocationByteCount();
        }
    }

    /**
     * A copy of the Android internals  insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     *
     * @see android.provider.MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String)
     */
    public static final String insertImage(ContentResolver cr,
                                           Bitmap source,
                                           String title,
                                           String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     *
     * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
     */
    private static final Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND, kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, (int) id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            String msg = ex.getLocalizedMessage();
            return null;
        }
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    /*
     * Upload the file
     */

    public void createFileEntry() {

        // create a new request client
        PSTClient request = new PSTClient(this, "", this);

        if (capturedType == 0) {

            try {
                Bitmap photo = BitmapFactory.decodeFile(filePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, baos); //photo is the bitmap object
                byte[] bitmapBytes = baos.toByteArray();
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(bitmapBytes, 0, bitmapBytes.length);
                hashCode = new BigInteger(1, md5.digest());
                sHashCode = String.valueOf(Math.abs(hashCode.longValue()));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                Calendar cal = Calendar.getInstance();
                sHashCode = sdf.format(cal.getTime());
                sUuid = UUID.randomUUID().toString();

                // create a key string
                fileKey = "content/school-" + groupId + "/entry-" + sHashCode + "/" + sUuid + ".jpg";

                // instantiate the json object
                entriesJson = new JSONObject();

                // build the entries map
                entriesJson.put("secret", secret);

                // Now add the tags the entries map
                tags = tags.replace("#", "");
                JSONArray tagsJArray = new JSONArray();
                String[] tagsArray = tags.split(" ");

                // Insert the tags
                if (tags.length() > 2) {
                    // Insert the tags
                    for (int i = 0; i < tagsArray.length; i++) {
                        JSONObject jo = new JSONObject();
                        jo.put("name", tagsArray[i]);
                        tagsJArray.put(jo);
                    }
                }

                entriesJson.put("tags", tagsJArray);
                entriesJson.put("content", "");
                entriesJson.put("type", "image");

                JSONArray mediaJArray = new JSONArray();
                JSONObject jo = new JSONObject();
                jo.put("path", fileKey);
                jo.put("width", (String.valueOf(photo.getWidth())));
                jo.put("height", (String.valueOf(photo.getHeight())));
                mediaJArray.put(jo);

                entriesJson.put("media", mediaJArray);

                // Setup and call the Simple Request object
                //request.uploadImage(fileKey,"POST",filePath,sUuid+"jpg");
                process = PSTProcessEnum.CREATE_ITEM_ENTRY;
                request.createItemEntry(groupId, memberId, entriesJson.toString(), token);

            } catch (JSONException je) {
                Log.d("Upload Photo failed!", je.getLocalizedMessage());
            } catch (NoSuchAlgorithmException nsae) {
                Log.d("Upload Photo failed!", nsae.getLocalizedMessage());
            }

        } else {

            try {
                byte[] bitmapBytes = IOUtils.toByteArray(new BufferedInputStream(new FileInputStream(filePath)));
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(bitmapBytes, 0, bitmapBytes.length);
                hashCode = new BigInteger(1, md5.digest());
                sHashCode = String.valueOf(Math.abs(hashCode.longValue()));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                Calendar cal = Calendar.getInstance();
                sHashCode = sdf.format(cal.getTime());
                sUuid = UUID.randomUUID().toString();

                // create a key string
                fileKey = "content/school-" + groupId + "/entry-" + sHashCode + "/" + sUuid + ".mp4";

                // instantiate the json object
                entriesJson = new JSONObject();

                // build the entires map
                entriesJson.put("secret", secret);

                // Now add the tags the entries map
                tags = tags.replace("#", "");
                JSONArray tagsJArray = new JSONArray();
                String[] tagsArray = tags.split(" ");

                if (tags.length() > 2) {
                    // Insert the tags
                    for (int i = 0; i < tagsArray.length; i++) {
                        JSONObject jo = new JSONObject();
                        jo.put("name", tagsArray[i]);
                        tagsJArray.put(jo);
                    }
                }

                entriesJson.put("tags", tagsJArray);
                entriesJson.put("content", "");
                entriesJson.put("type", "video");

                JSONArray mediaJArray = new JSONArray();
                JSONObject jo = new JSONObject();
                jo.put("path", fileKey);
                jo.put("width", 720);
                jo.put("height", 480);
                mediaJArray.put(jo);

                entriesJson.put("media", mediaJArray);

                process = PSTProcessEnum.CREATE_ITEM_ENTRY;
                request.createItemEntry(groupId, memberId, entriesJson.toString(), token);

            } catch (Exception ex) {
                Log.d("Upload video failed!", ex.getLocalizedMessage());
            }
        }
    }

    /*
     * Create all ICallbacks
     */
    @Override
    public void callbackProgress(int value) {
        Toast.makeText(getApplicationContext(), "Progress - " + value + "%", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void callbackComplete() {
        if (process == PSTProcessEnum.PUT_PHOTO) {
            Toast.makeText(getApplicationContext(), "Photo upload pending...", Toast.LENGTH_SHORT).show();
        }
        if (process == PSTProcessEnum.PUT_VIDEO) {
            Toast.makeText(getApplicationContext(), "Video upload pending...", Toast.LENGTH_SHORT).show();
        }
        if (process == PSTProcessEnum.SET_TAGS) {
            //Toast.makeText(getApplicationContext(), "Item tags set...", Toast.LENGTH_SHORT).show();
        }
        if (process == PSTProcessEnum.GET_ENTRY) {
            //Toast.makeText(getApplicationContext(), "Item entry retrieved...", Toast.LENGTH_SHORT).show();
        }
        if (process == PSTProcessEnum.CREATE_ITEM_ENTRY) {
            //Toast.makeText(getApplicationContext(), "Item entry created...", Toast.LENGTH_SHORT).show();
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
        } else {
            Toast.makeText(getApplicationContext(), "Transfer Error: " + error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void callbackResult(String result) {
        PSTClient request = new PSTClient(this, "", this);
        if (process == PSTProcessEnum.PUT_VIDEO) {
            //Toast.makeText(getApplicationContext(), "Video Uploaded...", Toast.LENGTH_SHORT).show();
        }
        if (process == PSTProcessEnum.PUT_PHOTO) {
            //Toast.makeText(getApplicationContext(), "Photo uploaded...", Toast.LENGTH_SHORT).show();
        }
        if (process == PSTProcessEnum.SET_TAGS) {
            Toast.makeText(getApplicationContext(), "Uploading file...", Toast.LENGTH_SHORT).show();
            if (capturedType == 0) {
                process = PSTProcessEnum.PUT_PHOTO;
                request.uploadImage(fileKey, filePath);
            } else {
                process = PSTProcessEnum.PUT_VIDEO;
                request.uploadVideo(fileKey, filePath);
            }
        }
        if (process == PSTProcessEnum.GET_ENTRY) {
            if (tags.length() > 2) {
                Toast.makeText(getApplicationContext(), "Setting tags...", Toast.LENGTH_SHORT).show();
                process = PSTProcessEnum.SET_TAGS;
                String newEntryId = String.valueOf(PSTPrivateFeedStorage.getNewEntryId());
                request.setTags(groupId, memberId, newEntryId, tagIds, token);
            } else {
                Toast.makeText(getApplicationContext(), "Uploading file...", Toast.LENGTH_SHORT).show();
                if (capturedType == 0) {
                    process = PSTProcessEnum.PUT_PHOTO;
                    request.uploadImage(fileKey, filePath);
                } else {
                    process = PSTProcessEnum.PUT_VIDEO;
                    request.uploadVideo(fileKey, filePath);
                }
            }
        }
        if (process == PSTProcessEnum.CREATE_ITEM_ENTRY) {
            Toast.makeText(getApplicationContext(), "Retrieving new entry...", Toast.LENGTH_SHORT).show();
            process = PSTProcessEnum.GET_ENTRY;
            String newEntryId = String.valueOf(PSTPrivateFeedStorage.getNewEntryId());
            request.getEntryById(memberId, groupId, year, newEntryId, token);
        }
    }

    @Override
    public void callbackResultObject(Object result) {
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
        }
        return super.onKeyDown(keyCode, event);
    }
}
