package com.peerstars.android.pststorage;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bmiller on 9/16/2015.
 */
public class PSTGroupsStorage {
    /*
     * This is the collection of all members of this group
     */
    static HashMap<Integer, HashMap<String, String>> sGroups = new HashMap<>();

    // create a new database table handler for groups
    public static PSTDatabaseTableGroups tableGroups = new PSTDatabaseTableGroups(PSTStorageHandler.getCurrentContext());

    public static void addGroup(int id, String name, String street, String city, String state) {
        HashMap<String, String> group = new HashMap<>();
        group.put("id", String.valueOf(id));
        group.put("name", name);
        group.put("street", street);
        group.put("city", city);
        group.put("state", state);
        sGroups.put(id, group);

        // create a group record in the database
        String address = group.get("street") + ", " + group.get("city") + ", " + group.get("state");
        PSTGroup dbgroup = new PSTGroup(Integer.parseInt(group.get("id")), group.get("name"), address, group.get("street"), group.get("city"), group.get("state"), "");
        tableGroups.addGroup(dbgroup);

    }

    public static void removeGroupById(Integer id) {
        sGroups.remove(id);
    }

    public static void updateGroup(int id, String name, String street, String city, String state) {
        HashMap<String, String> group = new HashMap<>();
        group.put("name", name);
        group.put("street", street);
        group.put("city", city);
        group.put("state", state);
        if (sGroups.containsKey(id)) {
            sGroups.remove(id);
        }
        sGroups.put(id, group);
    }

    public static HashMap<String, String> getGroup(int id) {
        if (sGroups.containsKey(id))
            return sGroups.get(id);
        return null;
    }

    public static String[] getGroupsNamesArray() {
        String[] namesArray = new String[sGroups.size()];
        int i = 0;
        for (Map.Entry<Integer, HashMap<String, String>> entry : sGroups.entrySet()) {
            HashMap<String, String> map = entry.getValue();
            namesArray[i++] = map.get("name");
        }
        return namesArray;
    }


    public static PSTGroup[] getGroupsArray() {
        PSTGroup[] groupsArray = new PSTGroup[sGroups.size()];
        int i = 0;
        for (Map.Entry<Integer, HashMap<String, String>> entry : sGroups.entrySet()) {
            HashMap<String, String> map = entry.getValue();
            String address = map.get("street") + ", " + map.get("city") + ", " + map.get("state");
            PSTGroup group = new PSTGroup(Integer.parseInt(map.get("id")), map.get("name"), address, map.get("street"), map.get("city"), map.get("state"), "");
            groupsArray[i++] = group;
        }
        return groupsArray;
    }

    public static PSTDatabaseTableGroups getGroupsDBTable(Context context) {
        return tableGroups;
    }

    public static void clear() {
        sGroups.clear();
    }
}
