package com.peerstars.android.pstconfiguration;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Created by bmiller on 9/2/2015.
 */
public class PSTConfiguration {

    // set the debug flag
    protected static boolean isDebug = false;

    // create all local storage variables
    protected static String baseProductionUrl = "https://peerstars-api-prod.herokuapp.com";
    protected static String baseStagingUrl = "https://peerstars-api-staging.herokuapp.com";

    // set constants for AWS S3 transactions
    protected static String aWSProductionUrl = "https://d270axt4tvonvm.cloudfront.net/";
    protected static String aWSStagingUrl = "https://d32t4nzeyynrri.cloudfront.net/";

    // set constants for the peerstars buckets
    protected static String prodBucket = "peerstars-prod";
    protected static String staggingBucket = "peerstars-staging";

    // set constants for the peerstars pool ids
    protected static String prodPoolId = "E2OF352K67NJQQ";
    protected static String staggingPoolId = "E1JG4VJMHUIC5S";

    // create user authentication token
    protected static String token = "";

    // getters and setters
    public static String getCurrentURL() {
        if (isDebug) {
            return getBaseStagingUrl();
        }
        return getBaseProductionUrl();
    }

    public static String getAWSCurrentURL() {
        if (isDebug) {
            return getaWSStagingUrl();
        }
        return getaWSProductionUrl();
    }

    public static String getCurrentBucket() {
        if (isDebug) {
            return getStagingBucket();
        }
        return getProdBucket();
    }

    public static String getCurrentPoolId() {
        if (isDebug) {
            return getStagingPoolId();
        }
        return getProdPoolId();
    }

    private static String getBaseProductionUrl() {
        return baseProductionUrl;
    }

    private static String getBaseStagingUrl() {
        return baseStagingUrl;
    }

    private static String getaWSProductionUrl() {
        return aWSProductionUrl;
    }

    private static String getaWSStagingUrl() {
        return aWSStagingUrl;
    }

    private static String getProdBucket() {
        return prodBucket;
    }

    private static String getStagingBucket() {
        return staggingBucket;
    }

    private static String getProdPoolId() {
        return prodPoolId;
    }

    private static String getStagingPoolId() {
        return staggingPoolId;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        token = token;
    }

    public static void setIsDebug(Context context) {
        int appFlags = context.getApplicationInfo().flags;
        isDebug = (appFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        isDebug = false;
    }
}
