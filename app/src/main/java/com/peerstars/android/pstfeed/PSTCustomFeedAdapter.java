package com.peerstars.android.pstfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.peerstars.android.R;
import com.peerstars.android.pstportraits.PSTCustomImageView;
import com.peerstars.android.pststorage.PSTMembersStorage;
import com.peerstars.android.pstutilities.ICallbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PSTCustomFeedAdapter extends BaseAdapter implements ICallbacks {

    Context context;

    PSTCustomFeedImageView picture;
    PSTCustomFeedVideoView movie;
    TextView providerName;
    TextView tags;
    TextView txtLifetime;
    ImageView thumbView;
    PSTCustomImageView providerView;
    TextView txtActions = null;
    TextView txtFlagged = null;
    ImageView videoicon;
    ProgressBar progressBar;

    String memberId = "";

    private final List<Item> mItems = new ArrayList<>();
    private final LayoutInflater mInflater;

    public PSTCustomFeedAdapter(Context context) {
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
        return mItems.get(i).drawableId;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        // Hold the work view
        View v;

        // Go get the next item
        Item item = getItem(i);

        if (item.type.equals("image")) {
            if (view == null || ((ViewGroup) view).getChildAt(0) instanceof PSTCustomFeedVideoView) {
                v = mInflater.inflate(R.layout.custom_feed_image_item, viewGroup, false);
            } else {
                v = view;
            }

            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.progressBar, v.findViewById(R.id.progressBar));
            picture = (PSTCustomFeedImageView) v.getTag(R.id.picture);

            if (picture == null)
                return null;

        } else if (item.type.equals("video")) {
            if (view == null || ((ViewGroup) view).getChildAt(0) instanceof PSTCustomFeedImageView) {
                v = mInflater.inflate(R.layout.custom_feed_video_item, viewGroup, false);
            } else {
                v = view;
            }

            v.setTag(R.id.movie, v.findViewById(R.id.movie));
            v.setTag(R.id.progressBar, v.findViewById(R.id.progressBar));
            movie = (PSTCustomFeedVideoView) v.getTag(R.id.movie);

            if (movie == null) {
                return null;
            }

        } else {
            v = view;
            return null;
        }

        v.setTag(R.id.providerName, v.findViewById(R.id.providerName));
        v.setTag(R.id.thumbView, v.findViewById(R.id.thumbView));
        v.setTag(R.id.providerImage, v.findViewById(R.id.providerImage));
        v.setTag(R.id.txtTags, v.findViewById(R.id.txtTags));
        v.setTag(R.id.txtLifetime, v.findViewById(R.id.txtLifetime));
        v.setTag(R.id.txtActions, v.findViewById(R.id.txtActions));
        v.setTag(R.id.flaggedView, v.findViewById(R.id.flaggedView));
        v.setTag(R.id.videoicon, v.findViewById(R.id.videoicon));

        providerName = (TextView) v.getTag(R.id.providerName);
        thumbView = (ImageView) v.getTag(R.id.thumbView);
        providerView = (PSTCustomImageView) v.getTag(R.id.providerImage);
        tags = (TextView) v.getTag(R.id.txtTags);
        txtLifetime = (TextView) v.getTag(R.id.txtLifetime);
        txtActions = (TextView) v.getTag(R.id.txtActions);
        txtFlagged = (TextView) v.getTag(R.id.flaggedView);
        videoicon = (ImageView) v.getTag(R.id.videoicon);
        progressBar = (ProgressBar) v.getTag(R.id.progressBar);

        Boolean ownedBy = Boolean.FALSE;
        if (item.memberId.equals(memberId))
            ownedBy = Boolean.TRUE;

        if (item.type.equals("image")) {
            if (picture == null) {
                return view;
            }

            // set the id
            picture.setIds(item.id, item.memberId, item.groupId);
            // set the urls in the custom view
            picture.setDisplayType(item.type);
            picture.setPhotoResizedUrl(item.photoResizedUrl);
            picture.setPhotoUrl(item.photoUrl);

            // initialize the thumbs for the feed
            picture.initContent(item.type, item.token, txtActions, ownedBy, txtFlagged, item.tags, progressBar, item.flagged, context);
        } else {

            // set the id
            movie.setIds(item.id, item.memberId, item.groupId);
            // set the urls in the custom view
            movie.setDisplayType(item.type);
            movie.setThumbnailUrl(item.thumbnailUrl);
            movie.setVideo360Url(item.video360Url);
            movie.setVideo480Url(item.video480Url);
            movie.setVideo720Url(item.video720Url);
            movie.setVideo1080Url(item.video1080Url);
            movie.setThumbView(thumbView);

            // initialize the thumbs for the feed
            movie.initContent(item.type, item.token, txtActions, ownedBy, txtFlagged, videoicon, item.tags, progressBar, item.flagged, context);

        }

        // set the values for the bottom banner
        providerName.setText(item.providerName);
        providerView.setPhotoUrl(item.providerPicUrl);
        providerView.initContent("roundBitmap", item.token);
        tags.setText(item.tags);
        txtLifetime.setText(item.lifetime);

        return v;
    }

    private static class Item {
        public final String id;
        public final String memberId;
        public final String groupId;
        public final String providerName;
        public final String providerPicUrl;
        public final int drawableId;
        public final String type;
        public final String photoResizedUrl;
        public final String photoUrl;
        public final String thumbnailUrl;
        public final String video360Url;
        public final String video480Url;
        public final String video720Url;
        public final String video1080Url;
        public final String tags;
        public final String lifetime;
        public final String flagged;
        public final String token;

        Item(
                String id,
                String memberId,
                String groupId,
                String providerName,
                String providerPicUrl,
                int drawableId,
                String type,
                String photoUrl,
                String photoResizedUrl,
                String thumbnailUrl,
                String video360Url,
                String video480Url,
                String video720Url,
                String video1080Url,
                String tags,
                String lifetime,
                String flagged,
                String token) {
            this.id = id;
            this.memberId = memberId;
            this.groupId = groupId;
            this.providerName = providerName;
            this.providerPicUrl = providerPicUrl;
            this.drawableId = drawableId;
            this.type = type;
            this.photoUrl = photoUrl;
            this.photoResizedUrl = photoResizedUrl;
            this.thumbnailUrl = thumbnailUrl;
            this.video360Url = video360Url;
            this.video480Url = video480Url;
            this.video720Url = video720Url;
            this.video1080Url = video1080Url;
            this.tags = tags;
            this.lifetime = lifetime;
            this.flagged = flagged;
            this.token = token;
        }
    }

    public String addItem(String[] params, String token) {

        /* params...
        id,
        memberId,
        groupId,
        type,
        urlStandard,
        urlResized,
        urlVideoThumb,
        urlVideo360,
        urlVideo480,
        urlVideo720,
        urlVideo1080,
        tagBuilder.toString(),
        memberId,
        groupId,
        lifetime,
        flagged};
         */

        PSTMembersStorage.groupId = params[2];
        PSTMembersStorage.token = token;

        String name = "";
        String providerPicUrl = "";
        try {
            name = (String) ((HashMap) PSTMembersStorage.getMember(Integer.parseInt(params[1]))).get("fname");
            name += " ";
            name += (String) ((HashMap) PSTMembersStorage.getMember(Integer.parseInt(params[1]))).get("lname");
            providerPicUrl = (String) ((HashMap) PSTMembersStorage.getMember(Integer.parseInt(params[1]))).get("photoUrl");
        } catch (Exception e) {
        }
        if (params[3].equals("image")) {
            mItems.add(
                    new Item(
                            params[0],
                            params[1],
                            params[2],
                            name,
                            providerPicUrl,
                            R.drawable.portraits96,
                            "image",
                            params[4],
                            params[5],
                            "",
                            "",
                            "",
                            "",
                            "",
                            params[11],
                            params[14],
                            params[15],
                            token));
        } else {
            mItems.add(
                    new Item(
                            params[0],
                            params[1],
                            params[2],
                            name,
                            providerPicUrl,
                            R.drawable.ic_launcher,
                            "video",
                            "",
                            "",
                            params[6],
                            params[7],
                            params[8],
                            params[9],
                            params[10],
                            params[11],
                            params[14],
                            params[15],
                            token));
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

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

}
