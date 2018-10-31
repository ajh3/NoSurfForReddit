package com.aaronhalbert.nosurfforreddit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import android.os.Build;

import androidx.lifecycle.ViewModel;

import android.text.Html;
import android.text.Spanned;

import com.aaronhalbert.nosurfforreddit.network.redditschema.Data_;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;
import com.aaronhalbert.nosurfforreddit.network.redditschema.Listing;
import com.aaronhalbert.nosurfforreddit.viewstate.CommentsViewState;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import java.util.Arrays;
import java.util.List;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class NoSurfViewModel extends ViewModel {
    private static final String USER_ABBREVIATION = "u/";
    private static final String BULLET_POINT = " \u2022 ";
    private static final String AUTO_MODERATOR = "AutoModerator";
    private static final String LINK_POST_DEFAULT_THUMBNAIL = "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_default_thumbnail_192";
    private static final String SELF_POST_DEFAULT_THUMBNAIL = "android.resource://com.aaronhalbert.nosurfforreddit/drawable/self_post_default_thumbnail_192";
    private static final String LINK_POST_NSFW_THUMBNAIL = "android.resource://com.aaronhalbert.nosurfforreddit/drawable/link_post_nsfw_thumbnail_192";
    private static final String DEFAULT = "default";
    private static final String SELF = "self";
    private static final String NSFW = "nsfw";
    private static final String IMAGE = "image";

    private NoSurfRepository repository;

    private LiveData<PostsViewState> stitchedAllPostsLiveDataViewState;
    private LiveData<PostsViewState> stitchedSubscribedPostsLiveDataViewState;
    private LiveData<CommentsViewState> commentsLiveDataViewState;

    private PostsViewState stitchedAllPostsCache = new PostsViewState();
    private PostsViewState stitchedSubscribedPostsCache = new PostsViewState();

    private String[] clickedPostIdsCache = new String[25];
    private LastClickedPostMetadata lastClickedPostMetadata;

    public NoSurfViewModel(NoSurfRepository repository) {
        this.repository = repository;
        stitchedAllPostsLiveDataViewState = stitchClickedPostIdsToPostsViewState(false);
        stitchedSubscribedPostsLiveDataViewState = stitchClickedPostIdsToPostsViewState(true);
        commentsLiveDataViewState = buildCommentsViewState();
    }

    // region network auth calls -------------------------------------------------------------------

    public void fetchUserOAuthTokenSync(String code) {
        repository.fetchUserOAuthTokenSync(code);
    }

    // endregion network auth calls ----------------------------------------------------------------

    // region network data calls -------------------------------------------------------------------

    public void fetchAllPostsSync() {
        repository.fetchAllPostsSync();
    }

    public void fetchSubscribedPostsSync() {
        repository.fetchSubscribedPostsSync();
    }

    public void fetchPostCommentsSync(String id) {
        repository.fetchPostCommentsSync(id);
    }

    // endregion network data calls ----------------------------------------------------------------

    // region init/de-init methods -----------------------------------------------------------------

    public void initApp() {
        repository.initializeTokensFromSharedPrefs();

        fetchAllPostsSync();
        fetchSubscribedPostsSync();
    }

    public void logUserOut() {
        repository.logUserOut();
    }

    // endregion init/de-init methods --------------------------------------------------------------

    // region event handling -----------------------------------------------------------------------

    public SingleLiveEvent<Boolean> getCommentsFinishedLoadingLiveEvents() {
        return repository.getCommentsFinishedLoadingLiveEvents();
    }

    public void consumeCommentsLiveDataChangedEvent() {
        repository.consumeCommentsLiveDataChangedEvent();
    }

    // endregion event handling --------------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    public LiveData<PostsViewState> getStitchedAllPostsLiveDataViewState() {
        return stitchedAllPostsLiveDataViewState;
    }

    public LiveData<PostsViewState> getStitchedSubscribedPostsLiveDataViewState() {
        return stitchedSubscribedPostsLiveDataViewState;
    }

    public LiveData<CommentsViewState> getCommentsLiveDataViewState() {
        return commentsLiveDataViewState;
    }

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return repository.getIsUserLoggedInLiveData();
    }

    public LastClickedPostMetadata getLastClickedPostMetadata() {
        return lastClickedPostMetadata;
    }

    // endregion getter methods --------------------------------------------------------------------

    // region setter methods -----------------------------------------------------------------------

    public void setLastClickedPostMetadata(LastClickedPostMetadata lastClickedPostMetadata) {
        this.lastClickedPostMetadata = lastClickedPostMetadata;
    }

    // endregion setter methods --------------------------------------------------------------------

    // region room methods and classes -------------------------------------------------------------

    public void insertClickedPostId(String id) {
        repository.insertClickedPostId(new ClickedPostId(id));
    }

    private LiveData<String[]> getClickedPostIdsLiveData() {
        return Transformations.map(repository.getClickedPostIdsLiveData(), input -> {
            int size = input.size();

            String[] clickedPostIds = new String[size];

            for (int i = 0; i < size; i++) {
                clickedPostIds[i] = input.get(i).getClickedPostId();
            }

            return clickedPostIds;
        });
    }

    // endregion room methods and classes ----------------------------------------------------------

    // region viewstate Transformations ------------------------------------------------------------

    private LiveData<CommentsViewState> buildCommentsViewState() {
        return Transformations.map(repository.getCommentsLiveData(), input -> {
            CommentsViewState commentsViewState;
            int autoModOffset;

            //check if there is at least 1 comment
            if (getNumTopLevelComments(input) > 0) {

                //calculate the number of valid comments after checking for & excluding AutoMod
                autoModOffset = calculateAutoModOffset(input);
                int numComments = getNumTopLevelComments(input) - autoModOffset;

                // only display first 3 top-level comments
                if (numComments > 3) numComments = 3;

                commentsViewState = new CommentsViewState(numComments);

                // construct the viewstate object
                for (int i = 0; i < numComments; i++) {
                    String commentAuthor = getCommentAuthor(input, autoModOffset + i);
                    int commentScore = getCommentScore(input, autoModOffset, i);

                    commentsViewState.commentBodies[i] = formatCommentBodyHtml(input, autoModOffset, i);
                    commentsViewState.commentDetails[i] = formatCommentDetails(commentAuthor, commentScore);
                }
            } else { //if zero comments
                commentsViewState = new CommentsViewState(0);
            }
            return commentsViewState;
        });
    }

    private LiveData<PostsViewState> buildPostsViewState(boolean isSubscribedPosts) {
        LiveData<Listing> postsLiveData;

        if (isSubscribedPosts) {
            postsLiveData = repository.getSubscribedPostsLiveData();
        } else {
            postsLiveData = repository.getAllPostsLiveData();
        }

        return Transformations.map(postsLiveData, input -> {
            PostsViewState postsViewState = new PostsViewState();

            for (int i = 0; i < 25; i++) {
                PostsViewState.PostDatum postDatum = new PostsViewState.PostDatum();

                Data_ data = input.getData().getChildren().get(i).getData();

                // both link posts and self posts share these attributes
                postDatum.isSelf = data.isIsSelf();
                postDatum.id = data.getId();
                postDatum.title = (decodeHtml(data.getTitle()).toString()); // some titles contain HTML special entities
                postDatum.author = data.getAuthor();
                postDatum.subreddit = data.getSubreddit();
                postDatum.score = data.getScore();
                postDatum.numComments = data.getNumComments();
                postDatum.thumbnailUrl = pickThumbnailUrl(data.getThumbnail());

                // assign link- and self-post specific attributes
                if (postDatum.isSelf) {
                    postDatum.selfTextHtml = formatSelfPostSelfTextHtml(data.getSelfTextHtml());
                } else {
                    postDatum.url = decodeHtml(data.getUrl()).toString();
                    postDatum.imageUrl = pickImageUrl(input, i);
                }

                postsViewState.postData.set(i, postDatum);
            }

            return postsViewState;
        });
    }

    private LiveData<PostsViewState> stitchClickedPostIdsToPostsViewState(boolean isSubscribedPosts){
        final MediatorLiveData<PostsViewState> mediator = new MediatorLiveData<>();

        LiveData<PostsViewState> postsLiveDataViewState;
        PostsViewState postsViewStateCache;

        if (isSubscribedPosts) {
            postsLiveDataViewState = buildPostsViewState(true);
            postsViewStateCache = stitchedSubscribedPostsCache;
        } else {
            postsLiveDataViewState = buildPostsViewState(false);;
            postsViewStateCache = stitchedAllPostsCache;
        }

        mediator.addSource(postsLiveDataViewState, postsViewState -> {

            for (int i = 0; i < 25; i++) {
                postsViewStateCache.postData.set(i, postsViewState.postData.get(i));

                updateCachedClickedPostIds(postsViewStateCache, i);
            }

            mediator.setValue(postsViewStateCache);
        });

        mediator.addSource(getClickedPostIdsLiveData(), strings -> {

            clickedPostIdsCache = strings;

            for (int i = 0; i < 25; i++) {
                updateCachedClickedPostIds(postsViewStateCache, i);
            }
        });

        return mediator;
    }

    // endregion viewstate Transformations ---------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void updateCachedClickedPostIds(PostsViewState postsViewStateCache, int i) {
        if (Arrays.asList(clickedPostIdsCache).contains(postsViewStateCache.postData.get(i).id)) {
            postsViewStateCache.hasBeenClicked[i] = true;
        }
    }

    private String formatSelfPostSelfTextHtml(String twiceEncodedSelfTextHtml) {
        if ((twiceEncodedSelfTextHtml != null) && !(twiceEncodedSelfTextHtml.equals(""))) {
            String onceEncodedSelfTextHtml = decodeHtml(twiceEncodedSelfTextHtml).toString();
            String decodedSelfTextHtml = decodeHtml(onceEncodedSelfTextHtml).toString();
            return (String) trimTrailingWhitespace(decodedSelfTextHtml);
        } else {
            return "";
        }
    }

    private Spanned formatCommentBodyHtml(List<Listing> input, int autoModOffset, int i) {
        String unescaped = getCommentBodyHtml(input, autoModOffset, i);
        Spanned escaped = decodeHtml(unescaped);

        return (Spanned) trimTrailingWhitespace(escaped);
    }

    private String pickImageUrl(Listing input, int i) {
        Data_ data = input.getData().getChildren().get(i).getData();

        if (data.getPreview() == null) {
            return LINK_POST_DEFAULT_THUMBNAIL;
        } else {
            String encodedImageUrl = data
                    .getPreview()
                    .getImages()
                    .get(0)
                    .getSource()
                    .getUrl();
            return decodeHtml(encodedImageUrl).toString();
        }
    }

    private String pickThumbnailUrl(String encodedThumbnailUrl) {
        String thumbnailUrl;

        switch (encodedThumbnailUrl) {
            case DEFAULT:
                thumbnailUrl = LINK_POST_DEFAULT_THUMBNAIL;
                break;
            case SELF:
                thumbnailUrl = SELF_POST_DEFAULT_THUMBNAIL;
                break;
            case NSFW:
                thumbnailUrl = LINK_POST_NSFW_THUMBNAIL;
                break;
            case IMAGE:
                thumbnailUrl = LINK_POST_DEFAULT_THUMBNAIL;
                break;
            default:
                thumbnailUrl = decodeHtml(encodedThumbnailUrl).toString();
                break;
        }
        return thumbnailUrl;
    }

    private int getNumTopLevelComments(List<Listing> input) {
        return input.get(1).getData().getChildren().size();
    }

    private boolean isFirstCommentByAutoMod(List<Listing> input) {
        return (getCommentAuthor(input, 0)).equals(AUTO_MODERATOR);
    }

    private int calculateAutoModOffset(List<Listing> input) {
        if (isFirstCommentByAutoMod(input)) {
            return 1;
        } else {
            return 0;
        }
    }

    private String getCommentAuthor(List<Listing> input, int i) {
        return input
                .get(1)
                .getData()
                .getChildren()
                .get(i)
                .getData()
                .getAuthor();
    }

    private int getCommentScore(List<Listing> input, int autoModOffset, int i) {
        return input
                .get(1)
                .getData()
                .getChildren()
                .get(autoModOffset + i)
                .getData()
                .getScore();
    }

    private String formatCommentDetails(String commentAuthor, int commentScore) {
        return USER_ABBREVIATION
                + commentAuthor
                + BULLET_POINT
                + Integer.toString(commentScore);
    }

    private String getCommentBodyHtml(List<Listing> input, int autoModOffset, int i) {
        String commentBodyHtml;

        if (Build.VERSION.SDK_INT >= 24) {
            commentBodyHtml = Html.fromHtml(input.get(1)
                    .getData()
                    .getChildren()
                    .get(autoModOffset + i)
                    .getData()
                    .getBodyHtml(), FROM_HTML_MODE_LEGACY).toString();
        } else {
            commentBodyHtml = Html.fromHtml(input.get(1)
                    .getData()
                    .getChildren()
                    .get(autoModOffset + i)
                    .getData()
                    .getBodyHtml()).toString();
        }
        return commentBodyHtml;
    }

    private Spanned decodeHtml(String encoded) {
        Spanned decodedHtml;

        if (Build.VERSION.SDK_INT >= 24) {
            decodedHtml = Html.fromHtml(encoded, FROM_HTML_MODE_LEGACY);
        } else {
            decodedHtml = Html.fromHtml(encoded);
        }

        return decodedHtml;
    }

    private CharSequence trimTrailingWhitespace(CharSequence source) {
        if (source == null) return "";

        int i = source.length();

        //decrement i and check if that character is whitespace
        do { --i; } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        //tick i up by 1 to return the full non-whitespace sequence
        return source.subSequence(0, i+1);
    }

    // endregion helper methods --------------------------------------------------------------------
}
