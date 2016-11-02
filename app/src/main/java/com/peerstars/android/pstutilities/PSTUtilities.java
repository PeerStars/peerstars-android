package com.peerstars.android.pstutilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bmiller on 9/1/2015.
 */
public class PSTUtilities {

    // declare all global objects
    private static Pattern pattern;
    private static Matcher matcher;

    private static int scrnWidth = 0;
    private static int scrnHeight = 0;


    // create a member name hash
    HashMap<Integer, String> _members = new HashMap<>();
    //Email Pattern
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Validate Email with regular expression
     *
     * @param email
     * @return true for Valid Email and false for Invalid Email
     */
    public static boolean validate(String email) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();

    }

    /**
     * Checks for Null String object
     *
     * @param txt
     * @return true for not null and false for null String object
     */
    public static boolean isNotNull(String txt) {
        return txt != null && txt.trim().length() > 0;
    }

    public static Dictionary getParamsObject() {

        // Instantiate Http Request Param Object
        Dictionary<String, String> params = new Dictionary<String, String>() {

            // create the Key/Value pair storage
            ArrayList<String[]> entries = new ArrayList<>();

            @Override
            public Enumeration<String> elements() {
                ArrayList<String> rsp = new ArrayList<>();
                if (entries.size() > 0) {
                    for (int i = 0; i < entries.size(); i++) {
                        // get the second element in the array
                        rsp.add(entries.get(i)[1]);
                    }
                }
                return Collections.enumeration(rsp);
            }

            @Override
            public String get(Object key) {
                if (key instanceof Integer) {
                    if (entries.contains(((Integer) key).intValue())) {
                        return entries.get(Integer.getInteger(key.toString()))[1];
                    }
                } else {
                    for (String[] entry : entries) {
                        if (entry[0].equals(key.toString())) {
                            return entry[1];
                        }
                    }
                }

                return null;
            }

            @Override
            public boolean isEmpty() {
                return size() < 1;
            }

            @Override
            public Enumeration<String> keys() {
                ArrayList<String> rsp = new ArrayList<>();
                if (entries.size() > 0) {
                    for (int i = 0; i < entries.size(); i++) {
                        rsp.add(entries.get(i)[0]);
                    }
                }
                return Collections.enumeration(rsp);
            }

            @Override
            public String put(String key, String value) {
                String[] entry = new String[2];
                if ((key.isEmpty())) throw new AssertionError("Key cannot be empty");
                if ((value.isEmpty())) throw new AssertionError("Value cannot be empty");
                entry[0] = key;
                entry[1] = value;
                entries.add(entry);
                return "Ok";
            }

            @Override
            public String remove(Object key) {
                for (String[] entry : entries) {
                    if (entry[0].equals(key.toString())) {
                        entries.remove(entry);
                    }
                }
                return "Ok";
            }

            @Override
            public int size() {
                return entries.size();
            }
        };

        // return the Dictionary object
        return params;
    }

    public static JSONObject getJsonObjectFromMap(Map params) throws JSONException {

        //all the passed parameters from the post request
        //iterator used to loop through all the parameters
        //passed in the post request
        Iterator iter = params.entrySet().iterator();

        //Stores JSON
        JSONObject holder = new JSONObject();

        //using the earlier example your first entry would get email
        //and the inner while would get the value which would be 'foo@bar.com'
        //{ fan: { email : 'foo@bar.com' } }

        //While there is another entry
        while (iter.hasNext()) {
            //gets an entry in the params
            Map.Entry pairs = (Map.Entry) iter.next();

            //creates a key for Map
            String key = (String) pairs.getKey();

            if (pairs.getValue() instanceof Map) {
                //Create a new map
                Map m = (Map) pairs.getValue();

                //object for storing Json
                JSONObject data = new JSONObject();

                //gets the value
                Iterator iter2 = m.entrySet().iterator();
                while (iter2.hasNext()) {
                    Map.Entry pairs2 = (Map.Entry) iter2.next();
                    data.put((String) pairs2.getKey(), pairs2.getValue());
                }

                //puts email and 'foo@bar.com'  together in map
                holder.put(key, data);

            } else {
                holder.put(key, pairs.getValue());
            }
        }
        return holder;
    }

    /*
     * set the title to custom font
     */
    public static SpannableString getCustomFont(String title, Context context) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new PSTTypeFaceSpan(context, "varsity_regular.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 250;
        int targetHeight = 250;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }

    public static void setScreenSize(int w, int h) {
        scrnHeight = h;
        scrnWidth = w;
    }

    public static int getScrnWidth() {
        return scrnWidth;
    }

    public static int getScrnHeight() {
        return scrnHeight;
    }
}
