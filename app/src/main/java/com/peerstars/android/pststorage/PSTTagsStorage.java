package com.peerstars.android.pststorage;

import java.util.HashMap;

/**
 * Created by bmiller on 9/16/2015.
 */
public class PSTTagsStorage {
    /*
     * This is the collection of all tags for this group
     */
    static HashMap<Integer, HashMap<String, String>> tags = new HashMap<>();

    static String tagsJson = "";

    public static void addTag(int id, String name, String checked) {
        HashMap<String, String> tag = new HashMap<>();
        tag.put("name", name);
        tag.put("checked", checked);
        tags.put(id, tag);

    }

    public static void removeTagById(Integer id) {
        tags.remove(id);
    }

    public static void updateTag(int id, String name, String checked) {
        if (tags.containsKey(id)) {
            tags.remove(id);
        }
        HashMap<String, String> tag = new HashMap<>();
        tag.put("name", name);
        tag.put("checked", checked);
        tags.put(id, tag);
    }

    public static HashMap<String, String> getTag(int id) {
        if (tags.containsKey(id))
            return tags.get(id);
        return null;
    }

    public static int size() {
        return tags.size();
    }

    public static void clear() {
        tags.clear();
    }

    public static void setTagsJson(String tagsJson) {
        PSTTagsStorage.tagsJson = tagsJson;
    }

    public static String getTagsJson() {
        return PSTTagsStorage.tagsJson;
    }
}
