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

        String details = context.getString(R.string.subreddit_abbreviation, getCurrentRedditListingObjectSubreddit(i))
                + context.getString(R.string.score, getCurrentRedditListingObjectScore(i))
                + context.getString(R.string.num_comments, getCurrentRedditListingObjectNumComments(i));

        postHolder.details.setText(details);

        GlideApp.with(context)
                .load(getCurrentRedditListingObjectThumbnail(i))
                .centerCrop()
                .into(postHolder.thumbnail);
    }

    class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title = null;
        TextView details = null;
        ImageView thumbnail = null;

        PostHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            details = itemView.findViewById(R.id.details);
            thumbnail = itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(this);

        }

        //TODO the isSelf checking is a giant mess, should not be checking here and in MainActivity just to avoid crash on null imageUrl
        @Override
        public void onClick(View v) {

            int i = getAdapterPosition();

            String currentPostType = getCurrentPostType(i);

            if (currentPostType.equals("self")) {
                recyclerViewOnClickCallback.launchSelfPost(getCurrentRedditListingObjectTitle(i),
                        getCurrentRedditListingObjectSelfTextHtml(i),
                        getCurrentRedditListingObjectId(i),
                        getCurrentRedditListingObjectSubreddit(i),
                        getCurrentRedditListingObjectAuthor(i),
                        getCurrentRedditListingObjectScore(i));


            } else {
                recyclerViewOnClickCallback.launchLinkPost(getCurrentRedditListingObjectTitle(i),
                        getCurrentRedditListingObjectImageUrl(i),
                        getCurrentRedditListingObjectUrl(i),
                        null,
                        getCurrentRedditListingObjectId(i),
                        getCurrentRedditListingObjectSubreddit(i),
                        getCurrentRedditListingObjectAuthor(i),
                        getCurrentRedditListingObjectScore(i));
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
        void launchSelfPost(String title, String selfText, String id, String subreddit, String author, int score);
        void launchLinkPost(String title, String imageUrl, String url, String gifUrl, String id, String subreddit, String author, int score);
    }


    private String getCurrentRedditListingObjectTitle(int i) {
        String title = currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getTitle();

        //decode post title in case it contains HTML special entities
        return decodeUrl(title);
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

        if (encodedThumbnail.equals("default")) {
            return "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_default_thumbnail_128";
        } else if (encodedThumbnail.equals("self")) {
            return "android.resource://com.aaronhalbert.nosurfforreddit/drawable/self_post_thumbnail_128";
        } else if (encodedThumbnail.equals("nsfw")) {
            return "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_nsfw_thumbnail_128";
        } else {
            return decodeUrl(encodedThumbnail);
        }
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

        if (currentListing.getData().getChildren().get(i).getData().getPreview() == null) {
            return "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_default_thumbnail_512";
        } else {
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
    }

    private String getCurrentRedditListingObjectSelfText(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getSelfText();

    }

    private String getCurrentRedditListingObjectSelfTextHtml(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getSelfTextHtml();

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

    private String getCurrentRedditListingObjectAuthor(int i) {
        return currentListing
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getAuthor();
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
        String decodedUrl;

        if (Build.VERSION.SDK_INT >= 24) {
            decodedUrl = (Html.fromHtml(url, Html.FROM_HTML_MODE_LEGACY)).toString();
        } else {
            decodedUrl = (Html.fromHtml(url).toString());
        }
        return decodedUrl;
    }

}
