package com.aaronhalbert.nosurfforreddit.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {

    private Listing currentListing = null;
    private RecyclerViewOnClickCallback recyclerViewOnClickCallback;
    private Context context;

    public PostsAdapter(Context context, RecyclerViewOnClickCallback recyclerViewOnClickCallback) {
        this.context = context;
        this.recyclerViewOnClickCallback = (PostsAdapter.RecyclerViewOnClickCallback) recyclerViewOnClickCallback;
    }

    public void setCurrentListing(Listing currentListing) {
        this.currentListing = currentListing;
    }

    @Override
    public int getItemCount() {
        if (currentListing != null) {
            return currentListing
                    .getData()
                    .getDist();
        } else return 0;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_single_post, viewGroup, false);

        return new PostHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PostHolder postHolder, int i) {
        postHolder.title.setText(getCurrentRedditListingObjectTitle(i));

        postHolder.subreddit.setText(context.getString(R.string.subreddit_abbreviation,
                getCurrentRedditListingObjectSubreddit(i)));

        postHolder.score.setText(context.getString(R.string.score,
                getCurrentRedditListingObjectScore(i)));

        postHolder.numComments.setText(context.getString(R.string.num_comments,
                getCurrentRedditListingObjectNumComments(i)));

        GlideApp.with(context)
                .load(getCurrentRedditListingObjectThumbnail(i))
                .centerCrop()
                .placeholder(R.drawable.textposticon64)
                .into(postHolder.thumbnail);
    }

    class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title = null;
        TextView subreddit = null;
        TextView score = null;
        TextView numComments = null;
        ImageView thumbnail = null;

        PostHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            subreddit = itemView.findViewById(R.id.subreddit);
            score = itemView.findViewById(R.id.score);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            numComments = itemView.findViewById(R.id.num_comments);

            itemView.setOnClickListener(this);

        }

        //TODO the isSelf checking is a giant mess, should not be checking here and in MainActivity just to avoid crash on null imageUrl
        @Override
        public void onClick(View v) {

            int i = getAdapterPosition();

            String currentPostType = getCurrentPostType(i);

            if (currentPostType.equals("self")) {
                recyclerViewOnClickCallback.launchSelfPost(getCurrentRedditListingObjectTitle(i),
                        getCurrentRedditListingObjectSelfText(i),
                        getCurrentRedditListingObjectId(i));


            } else {
                recyclerViewOnClickCallback.launchLinkPost(getCurrentRedditListingObjectTitle(i),
                        getCurrentRedditListingObjectImageUrl(i),
                        getCurrentRedditListingObjectUrl(i),
                        null,
                        getCurrentRedditListingObjectId(i));
            }

            /* TODO: finish this after implementing API access for imgur, gfycat
            switch (currentPostType) {
                case "self":
                    recyclerViewOnClickCallback.launchSelfPost(getCurrentRedditListingObjectTitle(i),
                            getCurrentRedditListingObjectSelfText(i));
                        break;
                case "gifv":


                case "gfycat":

                case "vreddit":

                case "otherLink":

                default: break;
            }
            */

        }
    }

    //TODO make sure this is implemented where it needs to be
    public interface RecyclerViewOnClickCallback {
        void launchSelfPost(String title, String selfText, String id);
        void launchLinkPost(String title, String imageUrl, String url, String gifUrl, String id);
    }


    private String getCurrentRedditListingObjectTitle(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getTitle();
    }

    private String getCurrentRedditListingObjectSubreddit(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getSubreddit();
    }

    private int getCurrentRedditListingObjectScore(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getScore();
    }

    private String getCurrentRedditListingObjectThumbnail(int i) {

        String encodedThumbnail = currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getThumbnail();

        return decodeUrl(encodedThumbnail);
    }

    private boolean getCurrentRedditListingObjectIsSelf(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .isIsSelf();
    }

    private String getCurrentRedditListingObjectUrl(int i) {

        String encodedObjectUrl = currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getUrl();

        return decodeUrl(encodedObjectUrl);
    }

    private String getCurrentRedditListingObjectImageUrl(int i) {

        String encodedImageUrl = currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getPreview()
                .getImages()
                .get(0)
                .getSource()
                .getUrl();

        return decodeUrl(encodedImageUrl);

    }

    private String getCurrentRedditListingObjectSelfText(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getSelfText();

    }

    private String getCurrentRedditListingObjectId(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getId();

    }

    private int getCurrentRedditListingObjectNumComments(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getNumComments();
    }

    private String getCurrentPostType(int i) {
        //TODO: convert to use enum

        if (getCurrentRedditListingObjectIsSelf(i)) {
            return "self";
        } else if (getCurrentRedditListingObjectUrl(i).contains(".gifv")) {
            return "gifv";
        } else if (getCurrentRedditListingObjectUrl(i).contains("gfycat")) {
            return "gfycat";
        } else if (getCurrentRedditListingObjectUrl(i).contains("v.redd.it")) {
            return "vreddit";
        } else {
            return "otherLink";
        }
    }

    private String getGifvVideoUrl(String url) {
        return url.replaceAll(".gifv", ".mp4");
    }

    private String getGfycatVideoUrl(String url) {
        return "hello";
    }

    private String getVredditVideoUrl(String url) {
        return "hello";
    }

    private String decodeUrl(String url) {
        if (Build.VERSION.SDK_INT >= 24) {
            url = (Html.fromHtml(url, Html.FROM_HTML_MODE_COMPACT)).toString();
        } else {
            url = (Html.fromHtml(url).toString());
        }
        return url;
    }

}
