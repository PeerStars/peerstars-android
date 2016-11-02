package com.peerstars.android.pststorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by bmiller on 10/29/2015.
 */
public class PSTDatabaseTableGroups {

    private static final String TAG = "DatabaseGroupsTable";

    //The columns we'll include in the groups table
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_STREET = "street";
    public static final String COL_CITY = "city";
    public static final String COL_STATE = "state";

    private static final String DATABASE_NAME = "PSTDATABASE";
    private static final String PST_VIRTUAL_TABLE = "PSTGROUPS";
    private static final int DATABASE_VERSION = 1;

    private final DatabaseOpenHelper mDatabaseOpenHelper;

    public PSTDatabaseTableGroups(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);

    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String PST_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + PST_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_ID + "  NOT NULL PRIMARY KEY, " +
                        COL_NAME + ", " +
                        COL_STREET + ", " +
                        COL_CITY + ", " +
                        COL_STATE + ")";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
            mDatabase = getReadableDatabase();
            mDatabase.delete(PST_VIRTUAL_TABLE, null, null);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(PST_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + PST_VIRTUAL_TABLE);
            onCreate(db);
        }

        public long getGroupCount() {
            Cursor cursor = mDatabase.rawQuery("SELECT COUNT (*) FROM PSTGROUPS", null);
            int count = 0;
            if (null != cursor)
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
            cursor.close();
            return count;
        }

        public long addGroup(int id, String name, String street, String city, String state) {

            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_ID, id);
            initialValues.put(COL_NAME, name);
            initialValues.put(COL_STREET, street);
            initialValues.put(COL_CITY, city);
            initialValues.put(COL_STATE, state);

            return mDatabase.insert(PST_VIRTUAL_TABLE, null, initialValues);
        }

        public Cursor getSuggestions(String filter) {
            String sql = "SELECT * FROM " + PST_VIRTUAL_TABLE + " WHERE name LIKE '%" + filter + "%' LIMIT 10";
            return mDatabase.rawQuery(sql, null);
        }

        public void bulkInsert(String records) {
            String sql = "INSERT INTO " + PST_VIRTUAL_TABLE + " VALUES (?,?,?,?,?);";
            SQLiteStatement statement = mDatabase.compileStatement(sql);

            try {
                //PSTGroupsStorage.clear();
                JSONArray groupset = new JSONArray(records);

                // create a transaction to handle the record input
                mDatabase.beginTransaction();
                for (int i = 0; i < groupset.length(); i++) {
                    JSONObject group = groupset.getJSONObject(i);
                    //PSTGroupsStorage.addGroup(group.getInt("id"), group.getString("name"), group.getString("street"), group.getString("city"),group.getString("state"));
                    // create a group record in the database
                    String address = group.get("street") + ", " + group.get("city") + ", " + group.get("state");
                    statement.clearBindings();
                    statement.bindLong(1, group.getInt("id"));
                    statement.bindString(2, group.getString("name"));
                    statement.bindString(3, group.getString("street"));
                    statement.bindString(4, group.getString("city"));
                    statement.bindString(5, group.getString("state"));
                    statement.execute();
                }
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();

            } catch (Exception je) {
                // To bad!!!
            }

        }
    }

    public void bulkInsert(String records) {
        mDatabaseOpenHelper.bulkInsert(records);
    }

    public void addGroup(PSTGroup group) {
        mDatabaseOpenHelper.addGroup(group.getId(), group.getName(), group.getStreet(), group.getCity(), group.getState());
    }

    public Cursor getSuggestions(String filter) {
        //String selection = COL_NAME + " LIKE ?";
        //String sql = "SELECT * FROM " + PST_VIRTUAL_TABLE + " WHERE name LIKE '%" + text + "%";
        //String[] selectionArgs = new String[] {"%" + text + "%"};
        //return query(selection, selectionArgs, null);

        if (mDatabaseOpenHelper.getGroupCount() < 1)
            return null;

        return mDatabaseOpenHelper.getSuggestions(filter);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(PST_VIRTUAL_TABLE);

        if (mDatabaseOpenHelper.getGroupCount() < 1)
            return null;

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
}
