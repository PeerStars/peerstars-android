package com.peerstars.android.pstportraits;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.peerstars.android.R;
import com.peerstars.android.pststorage.PSTDatabaseTableGroups;
import com.peerstars.android.pststorage.PSTGroup;

import java.util.ArrayList;
import java.util.List;

public class PSTCustomGroupAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<PSTGroup> mItems = new ArrayList<PSTGroup>();
    private PSTDatabaseTableGroups mtableGroups;

    public PSTCustomGroupAdapter(Context context, PSTDatabaseTableGroups tableGroups) {
        mContext = context;
        mtableGroups = tableGroups;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public PSTGroup getItem(int index) {
        return mItems.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.text1)).setText(getItem(position).getName());
        ((TextView) convertView.findViewById(R.id.text2)).setText(getItem(position).getAddress());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<PSTGroup> items = findItems(mContext, constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = items;
                    filterResults.count = items.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mItems = (List<PSTGroup>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;

    }

    /**
     * Returns a search result for the given book title.
     */
    private List<PSTGroup> findItems(Context context, String itemName) {
        // create the suggestions
        List<PSTGroup> suggestions = new ArrayList();
        Cursor cursor = mtableGroups.getSuggestions(itemName);
        int i = 0;
        while (cursor.moveToNext()) {
            String address = cursor.getString(2) + ", " + cursor.getString(3) + ", " + cursor.getString(4);
            PSTGroup g = new PSTGroup(cursor.getInt(0), cursor.getString(1), address);
            suggestions.add(g);
        }

        return suggestions;
    }

    public String addItem(String name, String address, int id) {
        mItems.add(new PSTGroup(id, name, address));
        return "ok";
    }

    public void clearItems() {
        mItems.clear();
    }

}
