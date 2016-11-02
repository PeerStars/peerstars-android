package com.peerstars.android.pststorage;

import com.peerstars.android.pstutilities.ICallbacks;

import java.util.HashMap;

/**
 * Created by bmiller on 9/16/2015.
 */
public class PSTMembersStorage implements ICallbacks {

    /*
     * This is the collection of all members of this group
     */
    static HashMap<Integer, HashMap<String, String>> members = new HashMap<>();

    // build a response buffer
    static StringBuffer response = new StringBuffer();

    // User token
    public static String token = "";

    // setup the current group to search
    public static String groupId = "";

    public static void addMember(int id, String fname, String lname, String gradYear, String photoUrl) {
        HashMap<String, String> member = new HashMap<>();
        member.put("fname", fname);
        member.put("lname", lname);
        member.put("gradYear", gradYear);
        member.put("photoUrl", photoUrl);
        members.put(id, member);

    }

    public static void removeMemberById(Integer id) {
        members.remove(id);
    }

    public static void updateMember(int id, String fname, String lname, String gradYear, String photoUrl) {
        HashMap<String, String> member = new HashMap<>();
        member.put("fname", fname);
        member.put("lname", lname);
        member.put("gradYear", gradYear);
        member.put("photoUrl", photoUrl);
        if (members.containsKey(id)) {
            removeMemberById(id);
        }
        addMember(id, fname, lname, gradYear, photoUrl);
    }

    public static HashMap<String, String> getMember(int id) {
        if (members.containsKey(id))
            return members.get(id);

        return null;
    }

    public static void clear() {
        members.clear();
    }

    @Override
    public void callbackProgress(int value) {
    }

    @Override
    public void callbackComplete() {

    }

    @Override
    public void callbackResult(String result) {

    }

    @Override
    public void callbackResultObject(Object result) {

    }

    @Override
    public void callbackCompleteWithError(String error) {

    }

}
