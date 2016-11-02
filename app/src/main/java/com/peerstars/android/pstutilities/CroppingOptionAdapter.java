package com.peerstars.android.pstutilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.peerstars.android.R;

import java.util.ArrayList;

/**
 * Created by bmiller on 10/15/2015.
 */

public class CroppingOptionAdapter extends ArrayAdapter {
    private ArrayList mOptions;
    private LayoutInflater mInflater;

    public CroppingOptionAdapter(Context context, ArrayList options) {
        super(context, R.layout.croping_selector, options);

        mOptions = options;

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.croping_selector, null);

        CroppingOption item = (CroppingOption) mOptions.get(position);

        if (item != null) {
            ((ImageView) convertView.findViewById(R.id.img_icon)).setImageDrawable(item.icon);
            ((TextView) convertView.findViewById(R.id.txt_name)).setText(item.title);

            return convertView;
        }

        return null;
    }
}

