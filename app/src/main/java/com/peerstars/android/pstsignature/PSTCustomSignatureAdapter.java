package com.peerstars.android.pstsignature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.peerstars.android.R;
import com.peerstars.android.pstportraits.PSTCustomImageView;
import com.peerstars.android.pststorage.PSTMembersStorage;
import com.peerstars.android.pstutilities.ICallbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PSTCustomSignatureAdapter extends BaseAdapter implements ICallbacks {

    Context context;

    private final List<Item> mItems = new ArrayList<>();
    private final LayoutInflater mInflater;

    private String token = "";

    public PSTCustomSignatureAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
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
        return mItems.get(i).id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        PSTCustomSignatureTextView text;
        PSTCustomSignatureVideoView movie;
        TextView providerName;
        TextView txtLifetime;
        ImageView thumbView;
        PSTCustomImageView providerView;
        TextView txtActions;
        TextView btnAccept;
        TextView btnDecline;

        // Go get the next item
        Item item = getItem(i);

        if (item.type.equals("text")) {
            if (view == null) {
                v = mInflater.inflate(R.layout.custom_signature_text_item, viewGroup, false);
            } else {
                if (view instanceof PSTCustomSignatureTextView) {
                    v = view;
                } else {
                    v = mInflater.inflate(R.layout.custom_signature_text_item, viewGroup, false);
                }
            }
        } else {
            if (view == null) {
                v = mInflater.inflate(R.layout.custom_signature_video_item, viewGroup, false);
            } else {
                if (view instanceof PSTCustomSignatureVideoView) {
                    v = view;
                } else {
                    v = mInflater.inflate(R.layout.custom_signature_video_item, viewGroup, false);
                }
            }
        }


        v.setTag(R.id.providerName, v.findViewById(R.id.providerName));
        v.setTag(R.id.text, v.findViewById(R.id.text));
        v.setTag(R.id.movie, v.findViewById(R.id.movie));
        v.setTag(R.id.thumbView, v.findViewById(R.id.thumbView));
        v.setTag(R.id.providerImage, v.findViewById(R.id.providerImage));
        v.setTag(R.id.txtLifetime, v.findViewById(R.id.txtLifetime));
        v.setTag(R.id.txtActions, v.findViewById(R.id.txtActions));
        v.setTag(R.id.btnAccept, v.findViewById(R.id.btnAccept));
        v.setTag(R.id.btnDecline, v.findViewById(R.id.btnDecline));

        text = (PSTCustomSignatureTextView) v.getTag(R.id.text);
        providerName = (TextView) v.getTag(R.id.providerName);
        movie = (PSTCustomSignatureVideoView) v.getTag(R.id.movie);
        thumbView = (ImageView) v.getTag(R.id.thumbView);
        providerView = (PSTCustomImageView) v.getTag(R.id.providerImage);
        txtLifetime = (TextView) v.getTag(R.id.txtLifetime);
        btnAccept = (TextView) v.getTag(R.id.btnAccept);
        btnDecline = (TextView) v.getTag(R.id.btnDecline);

        if (item.approved) {
            btnAccept.setVisibility(View.INVISIBLE);
            btnDecline.setVisibility(View.INVISIBLE);
        }
        if (item.type.equals("text")) {
            // set the urls in the custom view
            text.setText(item.content);

            // initialize the thumbs for the feed
            text.initContent(btnAccept, btnDecline, item.token, context);

        } else {

            // set the urls in the custom view
            movie.setDisplayType(item.type);
            movie.setThumbnailUrl(item.thumbnailUrl);
            movie.setVideo360Url(item.video360Url);
            movie.setVideo480Url(item.video480Url);
            movie.setVideo720Url(item.video720Url);
            movie.setVideo1080Url(item.video1080Url);
            movie.setThumbView(thumbView);

            // initialize the thumbs for the feed
            movie.initContent(btnAccept, btnDecline, item.token, context);

        }

        // set the values for the bottom banner
        providerName.setText(item.providerName);
        providerView.setPhotoUrl(item.providerPicUrl);
        providerView.initContent("roundBitmap", item.token);
        txtLifetime.setText(item.lifetime);

        return v;
    }

    private static class Item {
        public final String type;
        public final String status;
        public final String groupId;
        public final String providerName;
        public final String providerPicUrl;
        public final String content;
        public final String thumbnailUrl;
        public final String video360Url;
        public final String video480Url;
        public final String video720Url;
        public final String video1080Url;
        public final String memberId;
        public final String lifetime;
        public final String token;
        public final int id;
        public final Boolean approved;

        Item(
                String type,
                String status,
                String groupId,
                String providerName,
                String providerPicUrl,
                String content,
                String thumbnailUrl,
                String video360Url,
                String video480Url,
                String video720Url,
                String video1080Url,
                String memberId,
                String lifetime,
                String token,
                int id,
                Boolean approved) {
            this.type = type;
            this.status = status;
            this.groupId = groupId;
            this.providerName = providerName;
            this.providerPicUrl = providerPicUrl;
            this.content = content;
            this.thumbnailUrl = thumbnailUrl;
            this.video360Url = video360Url;
            this.video480Url = video480Url;
            this.video720Url = video720Url;
            this.video1080Url = video1080Url;
            this.memberId = memberId;
            this.lifetime = lifetime;
            this.token = token;
            this.id = id;
            this.approved = approved;
        }
    }

    public String addItem(String[] params, String token) {

        /* params...
                    type,               0
                    status,             1
                    groupId,            2
                    authorId,           3
                    content,            4
					urlVideoThumb,      5
					urlVideo360,        6
					urlVideo480,        7
					urlVideo720,        8
					urlVideo1080,       9
					memberId,           10
					lifetime};          11

        Item...
        public final String type;               0
        public final String status;             1
        public final String groupId;
        public final String providerName;       2
        public final String providerPicUrl;     3
        public final String content;            4
        public final String thumbnailUrl;       5
        public final String video360Url;        6
        public final String video480Url;
        public final String video720Url;
        public final String video1080Url;
        public final String lifetime;
        public final String token;
         */
        String name = "";
        String providerPicUrl = "";
        try {
            name = (String) ((HashMap) PSTMembersStorage.getMember(Integer.parseInt(params[3]))).get("fname");
            name += " ";
            name += (String) ((HashMap) PSTMembersStorage.getMember(Integer.parseInt(params[3]))).get("lname");
            providerPicUrl = (String) ((HashMap) PSTMembersStorage.getMember(Integer.parseInt(params[3]))).get("photoUrl");
        } catch (Exception e) {
            return "Failed!";
        }
        if (params[0].equals("text")) {
            mItems.add(
                    new Item(
                            params[0],
                            params[1],
                            params[2],
                            name,
                            providerPicUrl,
                            params[4],
                            "",
                            "",
                            "",
                            "",
                            "",
                            params[10],
                            params[11],
                            token,
                            0,
                            Boolean.parseBoolean(params[12])));
        } else {
            mItems.add(
                    new Item(
                            params[0],
                            params[1],
                            params[2],
                            name,
                            providerPicUrl,
                            "",
                            params[5],
                            params[6],
                            params[7],
                            params[8],
                            params[9],
                            params[10],
                            params[11],
                            token,
                            0,
                            Boolean.parseBoolean(params[12])));
        }
        return "ok";
    }

    public void clearItems() {
        mItems.clear();
    }


    @Override
    public void callbackProgress(int value) {

    }

    @Override
    public void callbackComplete() {
        // Toast.makeText(getApplicationContext(), "Get user data has completed!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackCompleteWithError(String error) {

    }

    @Override
    public void callbackResult(String result) {
        //Toast.makeText(getApplicationContext(), "Results: " + result, Toast.LENGTH_LONG).show();
    }

    @Override
    public void callbackResultObject(Object result) {
        // Toast.makeText(getApplicationContext(), "Get user data has returned an object: " + result.toString(), Toast.LENGTH_LONG);
    }


    public void onClick_AcceptBtn(View v) {

    }
}
