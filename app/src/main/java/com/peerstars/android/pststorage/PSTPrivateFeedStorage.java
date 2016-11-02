package com.peerstars.android.pststorage;

import java.util.HashMap;

/**
 * Created by bmiller on 9/16/2015.
 */
public class PSTPrivateFeedStorage {
    /*
     * This is the collection of all tags for this group
     */
    static HashMap<Integer, HashMap<String, String>> items = new HashMap<>();

    static int newEntryId = 0;

    static String itemsJson = "";

    public static void addItem(int id, String name, String checked) {
        HashMap<String, String> item = new HashMap<>();
        item.put("name", name);
        item.put("checked", checked);
        items.put(id, item);

    }

    public static void removeItemById(Integer id) {
        items.remove(id);
    }

    public static void updateItem(int id, String name, String checked) {
        if (items.containsKey(id)) {
            items.remove(id);
        }
        HashMap<String, String> item = new HashMap<>();
        item.put("name", name);
        item.put("checked", checked);
        items.put(id, item);
    }

    public static HashMap<String, String> getItem(int id) {
        if (items.containsKey(id))
            return items.get(id);
        return null;
    }

    public static int size() {
        return items.size();
    }

    public static void clear() {
        items.clear();
    }

    public static void setPrivateFeedJson(String tagsJson) {
        PSTPrivateFeedStorage.itemsJson = tagsJson;
    }

    public static void setNewEntryId(int id) {
        newEntryId = id;
    }

    public static int getNewEntryId() {
        return newEntryId;
    }

    public static String getPrivateFeedJson() {
        return PSTPrivateFeedStorage.itemsJson;
    }
}
