package com.peerstars.android.pstnetwork;

import android.content.Context;
import android.content.SharedPreferences;

import com.peerstars.android.pstauthentication.PSTAuthenticatedUser;
import com.peerstars.android.pstutilities.ICallbacks;
import com.peerstars.android.pstutilities.PSTUtilities;

import java.util.ArrayList;
import java.util.Dictionary;

// import the CognitoCredentialsProvider object from the auth package

/**
 * Created by bmiller on 8/26/2015.
 */
public class PSTClient implements ICallbacks {

    // store the context
    Context context;

    // Create an array of listeners
    private ArrayList<ICallbacks> listeners = new ArrayList<ICallbacks>();

    // create the shared preferences accessor
    public SharedPreferences settings;
    public SharedPreferences.Editor settingsHandler;


    // Create a token object
    String token = "";


    /*
     * Method that performs login authentication
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */
    public String createAccount(String groupId, String userdata) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("groupId", groupId);
        params.put("payload", userdata);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.CREATE_ACCOUNT);

        request.execute();

        return "Ok";
    }

    /*
     * Method that performs login authentication
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */
    public PSTAuthenticatedUser loginUser(String user, String password) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();
        PSTAuthenticatedUser pstAuthenticatedUser = new PSTAuthenticatedUser();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("username", user);
        params.put("password", password);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.LOGIN);

        request.execute();

        return pstAuthenticatedUser;
    }

    /*
     * Method that performs inappropriate flagging
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */

    public String flagAsInappropriate(String groupId, String memberId, String entryId, String token) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("groupId", groupId);
        params.put("memberId", memberId);
        params.put("entryId", entryId);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.FLAG_CONTENT);

        request.execute();

        return "Ok";
    }
    /*
     * Method that performs inappropriate flagging
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */

    public String deleteEntry(String groupId, String memberId, String entryId, String token) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("groupId", groupId);
        params.put("memberId", memberId);
        params.put("entryId", entryId);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.DELETE_ENTRY);

        request.execute();

        return "Ok";
    }


    /*
     * Method that performs set flags
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */
    public String setTags(String groupId, String memberId, String entryId, String tags, String token) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("groupId", groupId);
        params.put("memberId", memberId);
        params.put("entryId", entryId);
        params.put("tags", tags);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.SET_TAGS);

        request.execute();

        return "Ok";
    }

    /*
     * Method that performs login authentication
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */
    public String createItemEntry(String groupId, String memberId, String filedata, String token) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("groupId", groupId);
        params.put("memberId", memberId);
        params.put("payload", filedata);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.CREATE_ITEM_ENTRY);

        request.execute();

        return "Ok";
    }

    /*
 * Method that performs reset password
 *
 * @param user
 * @param password
 * @return PSTAuthenticatedUser
 */
    public String resetPassword(String payload) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();
        PSTAuthenticatedUser pstAuthenticatedUser = new PSTAuthenticatedUser();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("payload", payload);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.RESET_PASSWORD);

        request.execute();

        return "Ok";
    }

    /*
 * Method that uploads images
 *
 * @param user
 * @param password
 * @return PSTAuthenticatedUser
 */
    public void uploadImage(String key, String filePath) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("key", key);
        params.put("filePath", filePath);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.PUT_PHOTO);

        request.execute();
    }

    /*
    * Method that performs login authentication
    *
    * @param user
    * @param password
    * @return PSTAuthenticatedUser
    */
    public void uploadVideo(String key, String filePath) {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("filePath", filePath);
        params.put("fileKey", key);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.PUT_VIDEO);

        request.execute();
    }

    /*
     * Method that performs group loading
     */
    public String GetGroups() {
        PSTSimpleRequest requestHandler = new PSTSimpleRequest();

        // set this as a listener for the request handler
        requestHandler.addListener(this);

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_GROUPS);

        request.execute();

        return "Ok";
    }

    /*
     * Method that performs user data request
     *
     * @param user
     */
    public String getUserData(PSTAuthenticatedUser user) {

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();

        params.put("token", user.getToken());
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_USER_DATA);
        request.execute();

        return "";
    }

    /*
     * Method that performs get members request
     *
     * @param group
     * @param year
     * @param filters
     * @return PSTAuthenticatedUser
     */
    public String loadMembersUrls(String group, String year) {

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("token", token);
        params.put("group", group);
        params.put("year", year);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_MEMBERS);

        request.execute();

        return "";

    }


    /*
     * Method that performs get members request
     *
     * @param group
     * @param year
     * @param filters
     * @return PSTAuthenticatedUser
     */
    public String getMember(String group, String memberId, String token) {

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("token", token);
        params.put("group", group);
        params.put("member", memberId);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_MEMBER);

        request.execute();

        return "";

    }


    /*
     * Method that performs login authentication
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */
    public String loadFeedUrls(String group, String year, String filters, String limit) {

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("group", group);
        params.put("year", year);
        params.put("filters", filters);
        params.put("limit", limit);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_FEED_URLS);

        request.execute();

        return "";
    }


    /*
     * Method that performs login authentication
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */
    public String getEntryById(String member, String group, String year, String entryId, String token) {

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("member", member);
        params.put("group", group);
        params.put("year", year);
        params.put("entryId", entryId);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_ENTRY);

        request.execute();

        return "";
    }


    /*
     * Method that performs login authentication
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */
    public String loadSignatures(String group, String member) {

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("groupId", group);
        params.put("memberId", member);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_SIGNATURES);

        request.execute();

        return "";
    }

    /*
     * Method that performs login authentication
     *
     * @param user
     * @param password
     * @return PSTAuthenticatedUser
     */
    public String loadTags(String token, String groupId) {

        // create a simple request object
        PSTSimpleRequest request = new PSTSimpleRequest(this, context);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("groupId", groupId);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_TAGS);

        request.execute();

        return "";
    }

    /*
     * Method that gets images from the S3 service
     *
     * @param bucket
     * @param url
     */
    public String getImageFromUrl(String bucket, String url) {

        // create a simple request object
        PSTLongRunningRequest request = new PSTLongRunningRequest(this);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("bucket", bucket);
        params.put("url", url);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_IMAGE);

        request.execute();

        return "Ok";

    }

    /*
     * Method that gets images from the S3 service
     *
     * @param bucket
     * @param url
     */
    public String getVideoFromUrl(String bucket, String url) {

        // create a simple request object
        PSTLongRunningRequest request = new PSTLongRunningRequest(this);
        Dictionary<String, String> params = PSTUtilities.getParamsObject();
        params.put("bucket", bucket);
        params.put("url", url);
        params.put("token", token);
        request.setParameters(params);
        request.setProcess(PSTProcessEnum.GET_VIDEO);

        request.execute();

        return "Ok";

    }

    /*
     * Create a listener group handler
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
                case COMPLETE: // Complete
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
                case STRING_ERROR: // Result Errors
                    if (result instanceof String) {
                        l.callbackCompleteWithError((String) result);
                    } else {
                        l.callbackCompleteWithError("");
                    }
                    break;

            }
        }
    }


    @Override
    public void callbackProgress(int value) {
        notifyListeners(PSTCallBackEnum.PROGRESS, value);
    }

    @Override
    public void callbackComplete() {
        notifyListeners(PSTCallBackEnum.COMPLETE, null);
    }

    @Override
    public void callbackResult(String result) {
        notifyListeners(PSTCallBackEnum.STRING_RESULT, result);
    }

    @Override
    public void callbackResultObject(Object result) {
        notifyListeners(PSTCallBackEnum.OBJECT_RESULT, result);
    }

    @Override
    public void callbackCompleteWithError(String error) {
        notifyListeners(PSTCallBackEnum.STRING_ERROR, error);
    }

    // constructors
    public PSTClient(ICallbacks listener, String token, Context context) {
        addListener(listener);
        this.token = token;
        this.context = context;
    }
}