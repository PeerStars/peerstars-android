package com.peerstars.android.pstnetwork;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.IdentityChangedListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.peerstars.android.awscognito.AWSTransferObserverFactory;
import com.peerstars.android.pstconfiguration.PSTConfiguration;
import com.peerstars.android.pststorage.PSTDatabaseTableGroups;
import com.peerstars.android.pststorage.PSTPrivateFeedStorage;
import com.peerstars.android.pststorage.PSTStorageHandler;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by bmiller on 8/26/2015.
 */
public class PSTSimpleRequest extends AsyncTask<Void, Void, String> implements TransferListener {

    // build a context holder
    protected Context context;

    // Create an array of listeners
    private ArrayList<ICallbacks> listeners = new ArrayList<ICallbacks>();

    // create a parameters Dictionary
    Dictionary<String, String> args = PSTUtilities.getParamsObject();

    // create control flag that contains the process to do
    PSTProcessEnum process = PSTProcessEnum.LOGIN;

    // create a string response
    StringBuilder response = new StringBuilder();

    // creaet a PSTDatabaseTableGroups object
    PSTDatabaseTableGroups tableGroups;

    // We only need one instance of the clients and credentials provider
    private static AmazonS3Client sS3Client = null;
    private static CognitoCachingCredentialsProvider credentialsProvider = null;
    private static TransferUtility sTransferUtility = null;
    // of tokens for the provider
    private static Map<String, String> logins = new HashMap<String, String>();

    // A List of all transfers
    private static HashMap<Integer, TransferObserver> observers = new HashMap<>();

    // Cognito identity pool id
    protected static String cognitoIdentityPoolId = "us-east-1:f7aa75ea-335b-46dd-aac1-9097a8ffc064";
    protected static Regions region = Regions.US_EAST_1;

    // save total bytes of transfer
    long totalBytes = 0;

    @Override
    protected String doInBackground(Void... params) {

        String rc = "";

        switch (process) {
            case GET_GROUPS:
                rc = getGroups();
                break;
            case LOGIN:
                rc = loginUser();
                break;
            case LOGOUT:
                break;
            case GET_USER_DATA:
                rc = getUserData();
                break;
            case GET_MEMBERS:
                rc = getMembers();
                break;
            case GET_MEMBER:
                rc = getMembers();
                break;
            case GET_FEED_URLS:
                rc = getFeedUrls();
                break;
            case GET_ENTRY:
                rc = getEntryByID();
                break;
            case CREATE_ACCOUNT:
                rc = createUserAccount();
                break;
            case GET_SIGNATURES:
                rc = getSignatures();
                break;
            case GET_TAGS:
                rc = getTags();
                break;
            case SET_TAGS:
                rc = setTags();
                break;
            case RESET_PASSWORD:
                rc = resetPassword();
                break;
            case FLAG_CONTENT:
                rc = flagAsInappropriate();
                break;
            case PUT_PHOTO:
                rc = uploadImageToAWSS3();
                break;
            case PUT_VIDEO:
                rc = uploadVideoToAWSS3();
                break;
            case CREATE_ITEM_ENTRY:
                rc = createItemEntry();
                break;
            case DELETE_ENTRY:
                rc = deleteItemEntry();
                break;
        }

        return rc;

    }

    // custom background methods
    private String getGroups() {
        // create all necessary objects
        HttpsURLConnection con = null;

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request a user token
            url += "/schools";

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // open the connection
            con.connect();

            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            response.setLength(0);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            storeGroups(response.toString());

        } catch (Exception e) {
            response.append("Get Groups failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String getSignatures() {

        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request the member's signatures
            url += "/schools/" + args.get("groupId") + "/students/" + args.get("memberId") + "/signatures";

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();


            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            response.setLength(0);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }


        } catch (Exception e) {
            response.append("Get signatures with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String loginUser() {
        // create all necessary objects
        HttpsURLConnection con = null;
        String strUserPassword = "";
        byte[] encodedBytes = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request a user token
            url += "/token";

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // now add the user id and password parameters
            strUserPassword = args.get("username") + ":" + args.get("password");
            encodedBytes = Base64.encode(strUserPassword.getBytes(), Base64.DEFAULT);

            // we are using basic authentication
            basicHeaders = "Basic " + new String(encodedBytes);
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();

            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            response.setLength(0);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

        } catch (Exception e) {
            response.append("Login failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String createUserAccount() {
        // create all necessary objects
        HttpsURLConnection con = null;

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request a user token
            url += "/schools/" + args.get("groupId") + "/students";

            // create the JSON payload
            JSONObject payload = new JSONObject(args.get("payload"));

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // setup the post method
            con.setRequestProperty("Content-length", String.valueOf(args.get("payload").length()));
            con.setRequestProperty("Content-Type", "application/json");
            //con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setRequestProperty("Accept", "*/*");
            con.setDoOutput(true);
            con.setDoInput(true);

            // set the request method to "POST"
            con.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(payload.toString());
            wr.flush();

            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK || HttpResult == HttpURLConnection.HTTP_CREATED) {

                // create and input stream for the call
                InputStream is = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line = "";
                response.setLength(0);
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

            } else {
                response.setLength(0);
                response.append("Error: " + HttpResult);
            }

        } catch (Exception e) {
            response.append("Create failed with error: " + e.getMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String createItemEntry() {
        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request a user token
            url += "/schools/" + args.get("groupId") + "/students/" + args.get("memberId") + "/entries";

            // create the JSON payload
            JSONObject payload = new JSONObject(args.get("payload"));

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // setup the post method
            con.setRequestProperty("Content-length", String.valueOf(args.get("payload").length()));
            con.setRequestProperty("Content-Type", "application/json");
            //con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setRequestProperty("Accept", "*/*");
            con.setDoOutput(true);
            con.setDoInput(true);

            // set the request method to "POST"
            con.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(payload.toString());
            wr.flush();

            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK || HttpResult == HttpURLConnection.HTTP_CREATED) {

                // create and input stream for the call
                InputStream is = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line = "";
                response.setLength(0);
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                PSTPrivateFeedStorage.setNewEntryId(Integer.parseInt(con.getHeaderField("Location")));

            } else {
                response.setLength(0);
                response.append("Error: " + HttpResult);
            }

        } catch (Exception e) {
            response.append("Upload failed with error: " + e.getMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String deleteItemEntry() {
        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // flag as inappropriate
            url += "/schools/" + args.get("groupId") + "/students/" + args.get("memberId") + "/entries/" + args.get("entryId");

            // create the JSON payload
            JSONObject payload = new JSONObject();

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // setup the post method
            con.setRequestProperty("Content-length", String.valueOf(payload.toString().length()));
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setDoOutput(true);

            // set the request method to "DELETE"
            con.setRequestMethod("DELETE");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(payload.toString());
            wr.flush();

            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK || HttpResult == HttpURLConnection.HTTP_CREATED || HttpResult == HttpURLConnection.HTTP_NO_CONTENT) {

                // create and input stream for the call
                InputStream is = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line = "";
                response.setLength(0);
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

            } else {
                response.setLength(0);
                response.append("Error: " + HttpResult);
            }

        } catch (Exception e) {
            response.append("Error: " + e.getMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // Put image file up on the Amazon S3 server
    private String uploadImageToAWSS3() {
        String key = args.get("key");
        String filePath = args.get("filePath");

        AmazonS3 s3Client = AWSTransferObserverFactory.getsS3Client();

        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(PSTConfiguration.getCurrentBucket(), key, fileInputStream, objectMetadata);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            Log.i("Transfer result", result.getETag());
        } catch (FileNotFoundException fnf) {
            response.append("Error: " + fnf.getMessage());
            return "Error";
        } catch (AmazonClientException ace) {
            try {
                FileInputStream fileInputStream = new FileInputStream(filePath);
                ObjectMetadata objectMetadata = new ObjectMetadata();
                PutObjectRequest putObjectRequest = new PutObjectRequest(PSTConfiguration.getCurrentBucket(), key, fileInputStream, objectMetadata);
                PutObjectResult result = s3Client.putObject(putObjectRequest);
                Log.i("Transfer result", result.getETag());
            } catch (FileNotFoundException fnf2) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(filePath);
                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    PutObjectRequest putObjectRequest = new PutObjectRequest(PSTConfiguration.getCurrentBucket(), key, fileInputStream, objectMetadata);
                    PutObjectResult result = s3Client.putObject(putObjectRequest);
                    Log.i("Transfer result", result.getETag());
                } catch (FileNotFoundException fnf3) {
                    response.append("Error: " + fnf3.getMessage());
                    return "Error";
                }
            }
        }

        response.append("Success");
        return "Ok";
    }

    // Put video file up on the Amazon S3 server
    private String uploadVideoToAWSS3() {
        // create all necessary objects
        String fileKey = args.get("fileKey");
        String filePath = args.get("filePath");


        // AWSTransferObserverFactory.cancelAllTransfers(context);
        File videoFile = new File(filePath);

        //if(true) { // save in case I want to try pre signed url upload...

        TransferObserver observer
                = AWSTransferObserverFactory.getUploadObserver(
                PSTConfiguration.getCurrentBucket(), fileKey, videoFile);

        observer.setTransferListener(this);
        totalBytes = observer.getBytesTotal();

        /*} else {
            try {
                URL url = getPresignedURL(filePath, fileKey, "post");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("PUT");
                OutputStreamWriter out = new OutputStreamWriter(
                        connection.getOutputStream());
                out.write("This text uploaded as object.");
                out.close();
                int responseCode = connection.getResponseCode();
                Log.e("UPLoad to AWS", "Service returned response code " + responseCode);
            }catch (ProtocolException pe){
                Log.e("UPLoad to AWS","Service returned protocol error code " + pe.getLocalizedMessage());
            }catch (IOException ioe){
                Log.e("UPLoad to AWS","Service returned I/O error code " + ioe.getLocalizedMessage());
            }
        }*/

        response.append("Success");
        return "Ok";
    }

    // custom background methods
    private String setTags() {
        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request a user token
            url += "/schools/" + args.get("groupId") + "/students/" + args.get("memberId") + "/entries/" + args.get("entryId") + "/tags";

            // create the JSON payload
            JSONArray payload = new JSONArray(args.get("tags"));

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // setup the post method
            con.setRequestProperty("Content-length", String.valueOf(payload.toString().length()));
            con.setRequestProperty("Content-Type", "application/json");
            //con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setRequestProperty("Accept", "*/*");
            con.setDoOutput(true);
            con.setDoInput(true);

            // set the request method to "POST"
            con.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(payload.toString());
            wr.flush();

            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK
                    || HttpResult == HttpURLConnection.HTTP_CREATED) {

                // create and input stream for the call
                InputStream is = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line = "";
                response.setLength(0);
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

            } else if (HttpResult == HttpURLConnection.HTTP_NO_CONTENT) {

                response.setLength(0);
                response.append("Tags Set!");

            } else {
                response.setLength(0);
                response.append("Error: " + HttpResult);
            }

        } catch (Exception e) {
            response.append("Upload failed with error: " + e.getMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String resetPassword() {
        // create all necessary objects
        HttpsURLConnection con = null;

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request a user token
            url += "/resetpassword";

            // create the JSON payload
            JSONObject payload = new JSONObject(args.get("payload"));

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // setup the post method
            con.setRequestProperty("Content-length", String.valueOf(args.get("payload").length()));
            con.setRequestProperty("Content-Type", "application/json");
            //con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setRequestProperty("Accept", "*/*");
            con.setDoOutput(true);
            con.setDoInput(true);

            // set the request method to "POST"
            con.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(payload.toString());
            wr.flush();

            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK || HttpResult == HttpURLConnection.HTTP_CREATED || HttpResult == HttpURLConnection.HTTP_NO_CONTENT) {

                // create and input stream for the call
                InputStream is = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line = "";
                response.setLength(0);
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

            } else {
                response.setLength(0);
                response.append("Error: " + HttpResult);
            }

        } catch (Exception e) {
            response.append("Create failed with error: " + e.getMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String flagAsInappropriate() {
        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // flag as inappropriate
            url += "/schools/" + args.get("groupId") + "/students/" + args.get("memberId") + "/entries/" + args.get("entryId") + "/flag";

            // create the JSON payload
            JSONObject payload = new JSONObject();

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // setup the post method
            con.setRequestProperty("Content-length", String.valueOf(payload.toString().length()));
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            con.setDoOutput(true);

            // set the request method to "POST"
            con.setRequestMethod("PUT");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(payload.toString());
            wr.flush();

            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK || HttpResult == HttpURLConnection.HTTP_CREATED || HttpResult == HttpURLConnection.HTTP_NO_CONTENT) {

                // create and input stream for the call
                InputStream is = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line = "";
                response.setLength(0);
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

            } else {
                response.setLength(0);
                response.append("Error: " + HttpResult);
            }

        } catch (Exception e) {
            response.append("Error: " + e.getMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String getUserData() {
        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request a user token
            url += "/user";

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();


            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            response.setLength(0);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }


        } catch (Exception e) {
            response.append("Get User Data failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String getMembers() {
        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            if (process == PSTProcessEnum.GET_MEMBERS) {
                // request all members
                url += "/schools/" + args.get("group") + "/students?year=" + args.get("year") + "&limit=1000";
            } else if (process == PSTProcessEnum.GET_MEMBER) {
                url += "/schools/" + args.get("group") + "/students/" + args.get("member");
            }


            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();


            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            response.setLength(0);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }


        } catch (Exception e) {
            response.append("Get Members failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String getFeedUrls() {

        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // create a filters string
            String[] filters;

            if (args.get("filters").length() > 0) {
                filters = args.get("filters").toString().split(":");
                if (filters[0].equals("feed")) {
                    // build the request URL
                    url += "/schools/" + args.get("group") + "/entries?year=" + args.get("year") + "&limit=" + args.get("limit");
                }
                if (filters[0].equals("memberId") && filters[1].length() > 0) {
                    // build the request URL
                    url += "/schools/" + args.get("group") + "/students/" + filters[1] + "/feed?year=" + args.get("year");
                }
            } else {
                // build the request URL
                url += "/schools/" + args.get("group") + "/entries?year=" + args.get("year");
            }

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();


            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            response.setLength(0);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }


        } catch (Exception e) {
            response.append("Get feed items failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String getEntryByID() {

        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // build the request URL
            url += "/schools/" + args.get("group") + "/entries/" + args.get("entryId");

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();


            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            response.setLength(0);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }


        } catch (Exception e) {
            response.append("Get entry failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    // custom background methods
    private String getTags() {
        // create all necessary objects
        HttpsURLConnection con = null;
        String basicHeaders = "";

        try {

            // create a  new connection for login
            String url = PSTConfiguration.getCurrentURL();

            // request tags for the selected group
            url += "/schools/" + args.get("groupId") + "/tags";

            // create a SSL connection
            con = (HttpsURLConnection) (new URL(url)).openConnection();

            // set the authentication object
            basicHeaders = "Bearer " + new String(args.get("token"));
            con.setRequestProperty("Authorization", basicHeaders);

            // open the connection
            con.connect();

            // create and input stream for the call
            InputStream is = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            response.setLength(0);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }


        } catch (Exception e) {
            response.append("Get Tags failed with error: " + e.getLocalizedMessage());
            return "Error";
        } finally {
            if (con != null)
                con.disconnect();
        }

        return "Ok";
    }

    @Override
    protected void onPostExecute(String result) {
        if (process == PSTProcessEnum.GET_GROUPS) {
            // handle the result
            if (response.toString().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "Get Groups failed");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.OBJECT_RESULT, tableGroups);
            }
        } else if (process == PSTProcessEnum.CREATE_ACCOUNT) {
            // handle the result
            if (response.toString().isEmpty() || response.toString().startsWith("Create failed")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "Create Account failed");
            } else if (response.toString().startsWith("Error")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.PUT_PHOTO) {
            // handle the result
            if (response.toString().startsWith("Error")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.PUT_VIDEO) {
            // handle the result
            if (response.toString().startsWith("Error")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.CREATE_ITEM_ENTRY) {
            // handle the result
            if (response.toString().startsWith("Error")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.DELETE_ENTRY) {
            // handle the result
            if (response.toString().startsWith("Error")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.RESET_PASSWORD) {
            // handle the result
            if (response.toString().startsWith("Error")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
            }
        } else if (process == PSTProcessEnum.FLAG_CONTENT) {
            // handle the result
            if (response.toString().startsWith("Error")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
            }
        } else if (process == PSTProcessEnum.LOGIN) {
            // handle the result
            if (response.toString().toLowerCase().equals("unauthorized")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else if (response.toString().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "Login failed");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.LOGOUT) {
            // handle the result
            if (response.toString().toLowerCase().equals("unauthorized")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else if (response.toString().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "Login failed");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.GET_USER_DATA) {
            // handle the result
            if (response.toString().toLowerCase().equals("unauthorized")) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, response.toString());
            } else if (response.toString().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "Login failed");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.GET_MEMBERS) {
            // handle the result
            if (response.toString().toLowerCase().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "0");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.GET_MEMBER) {
            // handle the result
            if (response.toString().toLowerCase().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "0");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.GET_FEED_URLS) {
            // handle the result
            if (response.toString().toLowerCase().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "0");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.GET_ENTRY) {
            // handle the result
            if (response.toString().toLowerCase().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "0");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.GET_SIGNATURES) {
            // handle the result
            if (response.toString().toLowerCase().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "0");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.GET_TAGS) {
            // handle the result
            if (response.toString().toLowerCase().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "0");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        } else if (process == PSTProcessEnum.SET_TAGS) {
            // handle the result
            if (response.toString().toLowerCase().isEmpty()) {
                notifyListeners(PSTCallBackEnum.STRING_ERROR, "0");
            } else {
                notifyListeners(PSTCallBackEnum.COMPLETE, "");
                notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
            }
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        notifyListeners(PSTCallBackEnum.STRING_RESULT, response.toString());
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

    public void storeGroups(String values) {

        JSONArray groupset;
        JSONObject group;

        // create a new database table handler for groups
        tableGroups = new PSTDatabaseTableGroups(PSTStorageHandler.getCurrentContext());

        tableGroups.bulkInsert(values);

    }

    private URL getPresignedURL(String fileName, String key, String transType) {

        URL url = null;

        if (sS3Client == null) {
            // create an Amazon client
            sS3Client = new AmazonS3Client(getCognitoCashingCredentialsProvider(context));
        }
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(sS3Client, context);
        }

        try {
            Log.e("Pre signed URL", "Generating pre-signed URL.");

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(PSTConfiguration.getCurrentBucket(), key);
            if (transType.toLowerCase().equals("post")) {
                generatePresignedUrlRequest.setMethod(HttpMethod.PUT);
                generatePresignedUrlRequest.setContentType(getFileType(fileName));
            } else {
                generatePresignedUrlRequest.setMethod(HttpMethod.GET);
            }
            generatePresignedUrlRequest.setExpiration(new Date(System.currentTimeMillis() + 3600000)); // add an hour

            url = sS3Client.generatePresignedUrl(generatePresignedUrlRequest);

            System.out.println("Pre-Signed URL = " + url.toString());
        } catch (AmazonServiceException exception) {
            Log.e("Pre signed URL", "Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            Log.e("Pre signed URL", "Error Message: " + exception.getMessage());
            Log.e("Pre signed URL", "HTTP  Code: " + exception.getStatusCode());
            Log.e("Pre signed URL", "AWS Error Code:" + exception.getErrorCode());
            Log.e("Pre signed URL", "Error Type:    " + exception.getErrorType());
            Log.e("Pre signed URL", "Request ID:    " + exception.getRequestId());
        } catch (AmazonClientException ace) {
            Log.e("Pre signed URL", "Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, such as not being able to access the network.");
            Log.e("Pre signed URL", "Error Message: " + ace.getMessage());
        }

        return url;
    }

    public static String getFileType(String fileName) {
        if (fileName.toLowerCase().endsWith("jpg") || fileName.toLowerCase().endsWith("jpeg")) {
            return "image/jpeg";
        }
        if (fileName.toLowerCase().endsWith("png")) {
            return "image/png";
        }
        if (fileName.toLowerCase().endsWith("gif")) {
            return "image/gif";
        }
        if (fileName.toLowerCase().endsWith("avi")) {
            return "video/x-msvideo";
        }
        if (fileName.toLowerCase().endsWith("m4v")) {
            return "video/x-m4v";
        }
        if (fileName.toLowerCase().endsWith("mp4a")) {
            return "audio/mp4";
        }
        if (fileName.toLowerCase().endsWith("mp4")) {
            return "video/mp4";
        }
        if (fileName.toLowerCase().endsWith("oga")) {
            return "audio/ogg";
        }
        if (fileName.toLowerCase().endsWith("ogv")) {
            return "video/ogg";
        }

        return "";
    }

    public static CognitoCachingCredentialsProvider getCognitoCashingCredentialsProvider(Context context) {
        if (credentialsProvider == null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    cognitoIdentityPoolId,
                    region
            );
            credentialsProvider.clearCredentials();
            credentialsProvider.clear();

            credentialsProvider.registerIdentityChangedListener(new IdentityChangedListener() {
                @Override
                public void identityChanged(String oldIdentityId, String newIdentityId) {
                    String oi = oldIdentityId;
                    String ni = newIdentityId;
                }
            });
        }

        return credentialsProvider;
    }


    @Override
    public void onStateChanged(int id, TransferState state) {
        // do something
        Log.d("Current state: ", state.name());
        if (state.equals(TransferState.COMPLETED)) {
            notifyListeners(PSTCallBackEnum.STRING_RESULT, "Complete");
        }
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        int percentage = (int) (((double) bytesCurrent) / ((double) totalBytes) * 100);
        //Display percentage transferred to user
        notifyListeners(PSTCallBackEnum.PROGRESS, percentage);
        Log.d("Percentage complete:", String.valueOf(percentage));
    }

    @Override
    public void onError(int id, Exception ex) {
        // do something
        Log.e("Failed with error: ", ex.getLocalizedMessage());
        notifyListeners(PSTCallBackEnum.STRING_ERROR, ex.getLocalizedMessage());
    }

    // Constructors
    PSTSimpleRequest(ICallbacks listener, Context context) {
        this.addListener(listener);
        this.context = context;
    }

    PSTSimpleRequest() {
    }
}

