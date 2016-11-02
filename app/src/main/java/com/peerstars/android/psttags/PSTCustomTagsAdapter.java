package com.peerstars.android.psttags;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.peerstars.android.R;
import com.peerstars.android.pstfeed.PSTCustomFeedImageView;

import java.util.ArrayList;
import java.util.List;

public class PSTCustomTagsAdapter extends BaseAdapter {

    Context context;

    private final List<Item> mItems = new ArrayList<>();
    private final LayoutInflater mInflater;

    private String token = "";

    public PSTCustomTagsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v;
        CheckBox checkBox;

        // Go get the next item
        Item item = (Item) getItem(i);


        if (view == null) {
            v = mInflater.inflate(R.layout.custom_checkbox_item, viewGroup, false);
        } else {
            if (view instanceof PSTCustomFeedImageView) {
                v = view;
            } else {
                v = mInflater.inflate(R.layout.custom_checkbox_item, viewGroup, false);
            }
        }

        // get the checkbox and set it's properties
        v.setTag(R.id.chkTag, v.findViewById(R.id.chkTag));
        checkBox = (CheckBox) v.getTag(R.id.chkTag);
        checkBox.setChecked(false);
        if (item.value.equals("True"))
            checkBox.setChecked(true);

        return v;
    }

    private static class Item {
        public final String value;
        public final String isChecked;

        Item(
                String value,
                String isChecked
        ) {
            this.value = value;
            this.isChecked = isChecked;
        }
    }

    public String addItem(String value, String isChecked) {

        mItems.add(
                new Item(
                        value,
                        isChecked));

        return "ok";
    }

    public void clearItems() {
        mItems.clear();
    }

}
