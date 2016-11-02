package com.peerstars.android.pstnetwork;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.peerstars.android.pstconfiguration.PSTConfiguration;
import com.peerstars.android.pststorage.PSTContentCache;
import com.peerstars.android.pststorage.PSTStorageHandler;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by bmiller on 8/26/2015.
 */
public class PSTLongRunningRequest extends AsyncTask<Void, Void, String> implements TransferListener {

    // Create an array of listeners
    private ArrayList<ICallbacks> listeners = new ArrayList<ICallbacks>();

    // create a parameters Dictionary
    Dictionary<String, String> args = PSTUtilities.getParamsObject();

    // create control flag that contains the process to do
    PSTProcessEnum process = PSTProcessEnum.LOGIN;

    // create a string response
    StringBuilder response = new StringBuilder();

    // create a progress update field
    String progress = "0%";

    // create a image/video file for the download
    File responseFile = null;
    Bitmap responseBitmap = null;

    @Override
    protected String doInBackground(Void... params) {

        String rc = "";

        switch (process) {
            case GET_IMAGE:
                rc = getImageFromUrl();
                break;
            case GET_VIDEO:
                rc = getVideoFromUrl();
                break;
        }

        return rc;

    }

 /*   // custom background methods
    public String getImageFromAWSClient() {

        try {

            // request a user token
            //String url = PSTConfiguration.getaWSProductionUrl() + args.get("url");
            String url = args.get("url");

            if (PSTStorageHandler.isExternatStorageWritable()) {
                // create the new response file
                Calendar cal = Calendar.getInstance();
                responseFile = new File(PSTStorageHandler.getFileDir(), "ps-" + cal.getTimeInMillis() + ".jpg");
                responseFile.createNewFile();
            } else {
                response.append("Error: download failed - external storage is not available");
                return "Error: download failed!";
            }

            // create a new transfer observer
            String bucket = args.get("bucket");
            TransferObserver downloadObserver = AWSTransferObserverFactory.getDownloadObserver(bucket, url, responseFile, );
            downloadObserver.setTransferListener(this);

        } catch (Exception e) {
            response.append("Error: Get image failed with error: " + e.getLocalizedMessage());
            return "Error";
        }

        return "Ok";
    }*/

    // custom background methods
    private String getImageFromUrl() {

        if (PSTContentCache.isInitialized()) { //&&!args.get("url").toLowerCase().contains("profile-photo")){
            responseBitmap = PSTContentCache.getBitmapFromMemCache(args.get("url"));
            if (responseBitmap != null)
                return "Ok";
        }
        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getAWSCurrentURL();

            // request a user token
            url += args.get("url");

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();

            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            Bitmap bm = BitmapFactory.decodeStream(is);

            // set the response to the bitmap file
            responseBitmap = bm;

            // write it to the cache
            PSTContentCache.addBitmapToMemoryCache(args.get("url"), bm);

        } catch (Exception e) {
            response.append("Get image failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            con.disconnect();
        }

        return "Ok";
    }


    // custom background methods
    private String getVideoFromUrl() {

        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getAWSCurrentURL();

            // request a user token
            url += args.get("url");

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // create the new response file
            Calendar cal = Calendar.getInstance();
            responseFile = new File(PSTStorageHandler.getFileDir(), "ps-" + cal.getTimeInMillis() + ".mp4");
            responseFile.createNewFile();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();

            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());

            //this is the total size of the file which we are downloading
            int totalSize = con.getContentLength();
            int downloadedSize = 0;


            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            FileOutputStream fos = new FileOutputStream(responseFile);

            while ((bufferLength = is.read(buffer)) > 0) {
                fos.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            //close the output stream when complete //
            fos.close();

        } catch (Exception e) {
            response.append("Get image failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            con.disconnect();
        }

        return "Ok";
    }

    @Override
    protected void onPostExecute(String result) {
        if (process == PSTProcessEnum.GET_IMAGE) {
            // handle the result
            if (response.toString().toLowerCase().startsWith("error:")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
                notifyListeners(PSTCallBackEnum.OBJECT_RESULT, responseBitmap);
            }
        }
        if (process == PSTProcessEnum.GET_VIDEO) {
            // handle the result
            if (response.toString().toLowerCase().startsWith("error:")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
                notifyListeners(PSTCallBackEnum.OBJECT_RESULT, responseFile);
            }
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    //AWS methods
    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        progress = String.valueOf(bytesCurrent / bytesTotal) + "%";
    }

    @Override
    public void onError(int id, Exception ex) {
        progress = "Error: " + ex.getLocalizedMessage();
    }

    @Override
    public void onStateChanged(int id, TransferState state) {
        if (state == TransferState.COMPLETED) {
            response.append("Complete");
            if (process == PSTProcessEnum.GET_IMAGE) {
                // handle the result
                if (response.toString().toLowerCase().startsWith("error:")) {
                    notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
                } else {
                    notifyListeners(PSTCallBackEnum.COMPLETE, "");
                    notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
                    notifyListeners(PSTCallBackEnum.OBJECT_RESULT, responseFile);
                }
            }
            if (process == PSTProcessEnum.GET_VIDEO) {
                // handle the result
                if (response.toString().toLowerCase().startsWith("error:")) {
                    notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
                } else {
                    notifyListeners(PSTCallBackEnum.COMPLETE, "");
                    notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
                    notifyListeners(PSTCallBackEnum.OBJECT_RESULT, responseFile);
                }
            }
        }
        if (state == TransferState.FAILED) {
            response.append("Error: download failed!");
        }
        if (state == TransferState.IN_PROGRESS) {
            progress = state.name();
        }


    }

    // This section replies back to the parent during and after the requests

    /*
     * Add a listener to the group
     */
    public void addListener(ICallbacks listener) {
        this.listeners.add(listener);
    }

    /*
     * Remove a listener from the group
     */
    public void removeListener(ICallbacks listener) {
        this.listeners.remove(listener);
    }

    /*
     * Fire the notify listeners action
     */
    private void notifyListeners(PSTCallBackEnum type, Object result) {
        for (int i = 0; i < listeners.size(); i++) {
            ICallbacks l = listeners.get(i);
            switch (type) {
                case PROGRESS: // Progress
                    if (result instanceof Integer) {
                        l.callbackProgress(((Integer) result).intValue());
                    } else {
                        l.callbackProgress(0);
                    }
                    break;
                case COMPLETE:
                    // Complete
                    l.callbackComplete();
                    break;
                case STRING_RESULT: // Result String
                    if (result instanceof String) {
                        l.callbackResult((String) result);
                    } else {
                        l.callbackResult("");
                    }
                    break;
                case OBJECT_RESULT: // Result Object
                    l.callbackResultObject(result);
                    break;
                case STRING_ERROR: // String Error
                    if (result instanceof String) {
                        l.callbackCompleteWithError((String) result);
                    } else {
                        l.callbackCompleteWithError("");
                    }
                    break;

            }
        }
    }

    // Getters and Setters
    public void setParameters(Dictionary<String, String> parameters) {
        this.args = parameters;
    }

    public void setProcess(PSTProcessEnum process) {
        this.process = process;
    }

    // Constructors
    PSTLongRunningRequest(ICallbacks listener) {
        this.addListener(listener);
    }

    PSTLongRunningRequest() {
    }
}

