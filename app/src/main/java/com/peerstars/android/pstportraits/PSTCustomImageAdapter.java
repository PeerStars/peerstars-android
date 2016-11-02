package com.peerstars.android.pstportraits;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.peerstars.android.R;
import com.peerstars.android.pststorage.PSTMembersStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PSTCustomImageAdapter extends BaseAdapter {

    private final List<Item> mItems = new ArrayList<Item>();
    private final LayoutInflater mInflater;

    public PSTCustomImageAdapter(Context context) {
        mInflater = LayoutInflater.from(context);

        // mItems.add(new Item("No Members found!", R.drawable.profile_button96, ""));

    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Item getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mItems.get(i).drawableId;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        TextView grad;
        PSTCustomImageView picture;
        TextView name;

        if (v == null) {
            v = mInflater.inflate(R.layout.custom_portrait_item, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));
            v.setTag(R.id.txtGrad, v.findViewById(R.id.txtGrad));
        }

        grad = (TextView) v.getTag(R.id.txtGrad);
        picture = (PSTCustomImageView) v.getTag(R.id.picture);
        name = (TextView) v.getTag(R.id.text);

        Item item = getItem(i);

        // set the necessary values for the portrait gallery
        grad.setText("'" + item.gradYear.substring(2));
        picture.setGradYear(item.gradYear);
        picture.setPhotoUrl(item.photoUrl);

        // initialize the pics for the portraits
        picture.initContent(item.type, item.token);

        name.setText(item.name);

        picture.setClickable(true);

        return v;
    }

    private static class Item {
        public final String name;
        public final int drawableId;
        public final String type;
        public final String gradYear;
        public final String photoUrl;
        public final String token;

        Item(String name, int drawableId, String type, String gradYear, String photoUrl, String token) {
            this.name = name;
            this.drawableId = drawableId;
            this.type = type;
            this.gradYear = gradYear;
            this.photoUrl = photoUrl;
            this.token = token;
        }
    }


    public String addItem(String[] params, String token) {

        /* params...
        id,
        firstName,
        lastName,
        gradYear,
        photoUrl
         */
        String name = (String) ((HashMap) PSTMembersStorage.getMember(Integer.parseInt(params[0]))).get("fname");
        name += " ";
        name += (String) ((HashMap) PSTMembersStorage.getMember(Integer.parseInt(params[0]))).get("lname");
        if (params[4].length() > 0) {
            mItems.add(new Item(name, R.drawable.portraits96, "image", params[3], params[4], token));
        }
        return "ok";
    }

    public void clearItems() {
        mItems.clear();
    }

}
