package com.aaronhalbert.nosurfforreddit.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.GlideApp;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.databinding.RowSinglePostBinding;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.Arrays;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {

    private Listing currentListing = null;
    private RecyclerViewOnClickCallback recyclerViewOnClickCallback;
    private LoadListOfReadPostIds loadListOfReadPostIds;
    private Context context; //TODO: convert to activity


    public PostsAdapter(Context context, RecyclerViewOnClickCallback recyclerViewOnClickCallback, LoadListOfReadPostIds loadListOfReadPostIds) {
        this.context = context;
        this.recyclerViewOnClickCallback = (PostsAdapter.RecyclerViewOnClickCallback) recyclerViewOnClickCallback;
        this.loadListOfReadPostIds = loadListOfReadPostIds;
    }

    public void setCurrentListing(Listing currentListing) { //TODO: pull this into the constructor instead
        this.currentListing = currentListing;
    }

    @Override
    public int getItemCount() {
        if (currentListing != null) {
            return currentListing.getData().getDist();
        } else return 0;
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowSinglePostBinding rowSinglePostBinding = RowSinglePostBinding.inflate(((Activity) context).getLayoutInflater(), parent, false);

        return new RowHolder(rowSinglePostBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder rowHolder, int position) {
        rowHolder.bindModel(currentListing, position);

        GlideApp.with(context)
                .load(getCurrentRedditListingObjectThumbnail(position))
                .centerCrop()
                .into(rowHolder.rowSinglePostBinding.thumbnail);

        String[] readPostIds = loadListOfReadPostIds.getReadPostIds();

        if (Arrays.asList(readPostIds).contains(getCurrentRedditListingObjectId(position))) {
            rowHolder.rowSinglePostBinding.title.setPaintFlags(rowHolder.rowSinglePostBinding.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            rowHolder.rowSinglePostBinding.title.setPaintFlags(rowHolder.rowSinglePostBinding.title.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    public class RowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final RowSinglePostBinding rowSinglePostBinding;
        int position;

        RowHolder(RowSinglePostBinding rowSinglePostBinding) {
            super(rowSinglePostBinding.getRoot());
            this.rowSinglePostBinding = rowSinglePostBinding;
            itemView.setOnClickListener(this);
        }

        void bindModel(Listing listing, int position) {
            this.position = position;
            rowSinglePostBinding.setListing(listing);
            rowSinglePostBinding.setController(this); // to call onClick
            rowSinglePostBinding.executePendingBindings();
        }



        //TODO the isSelf checking is a giant mess, should not be checking here and in MainActivity just to avoid crash on null imageUrl
        @Override
        public void onClick(View v) {

            int i = getAdapterPosition();
            loadListOfReadPostIds.setLastClickedRow(i);


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
        }
    }











    //TODO: pull out methods being re-used here

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
            return "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_default_thumbnail_192";
        } else if (encodedThumbnail.equals("self")) {
            return "android.resource://com.aaronhalbert.nosurfforreddit/drawable/self_post_default_thumbnail_192";
        } else if (encodedThumbnail.equals("nsfw")) {
            return "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_nsfw_thumbnail_192";
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
            return "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_default_thumbnail_192";
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


    private String decodeUrl(String url) {
        String decodedUrl;

        if (Build.VERSION.SDK_INT >= 24) {
            decodedUrl = (Html.fromHtml(url, Html.FROM_HTML_MODE_LEGACY)).toString();
        } else {
            decodedUrl = (Html.fromHtml(url).toString());
        }
        return decodedUrl;
    }

    //TODO make sure this is implemented where it needs to be
    public interface RecyclerViewOnClickCallback {
        void launchSelfPost(String title, String selfText, String id, String subreddit, String author, int score);
        void launchLinkPost(String title, String imageUrl, String url, String gifUrl, String id, String subreddit, String author, int score);
    }

    public interface LoadListOfReadPostIds {
        String[] getReadPostIds();
        void setLastClickedRow(int lastClickedRow);
    }

}
