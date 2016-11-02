package com.peerstars.android.pststorage;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by bmiller on 9/24/2015.
 */
public class PSTStorageHandler {

    // store the app context
    protected static Context context;

    // setup the external storage state
    protected static boolean mExternalStorageIsAvailable = false;
    protected static boolean mExternalStorageIsWritable = false;

    // hold the external storage dir
    protected static String FILE_DIR;

    public static void setContext(Context ctx) {
        context = ctx;
    }

    public static Context getCurrentContext() {
        return context;
    }

    public static void setFileDir(String fileDir) {
        FILE_DIR = fileDir;
    }

    public static String getFileDir() {
        if (isExternatStorageWritable()) {
            File file = new File(FILE_DIR + "/peerstars");
            file.mkdirs();
            return FILE_DIR + "/peerstars";
        }
        return "";
    }

    public static boolean isExternatStorageWritable() {
        updateExternalStorageState();
        return mExternalStorageIsAvailable && mExternalStorageIsWritable;
    }

    protected static void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageIsAvailable = mExternalStorageIsWritable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageIsAvailable = true;
            mExternalStorageIsWritable = false;
        } else {
            mExternalStorageIsAvailable = mExternalStorageIsWritable = false;
        }
    }
}
