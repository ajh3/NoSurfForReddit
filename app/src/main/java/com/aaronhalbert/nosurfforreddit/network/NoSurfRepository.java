package com.aaronhalbert.nosurfforreddit.network;

import androidx.core.text.HtmlCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.text.Spanned;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.network.redditschema.Data_;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostIdDao;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostIdRoomDatabase;
import com.aaronhalbert.nosurfforreddit.network.redditschema.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.network.redditschema.Listing;
import com.aaronhalbert.nosurfforreddit.network.redditschema.UserOAuthToken;
import com.aaronhalbert.nosurfforreddit.room.InsertClickedPostIdThreadPoolExecutor;
import com.aaronhalbert.nosurfforreddit.viewstate.CommentsViewState;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.Transformations;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NoSurfRepository {
    private static final String APP_ONLY_GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private static final String USER_GRANT_TYPE = "authorization_code";
    private static final String USER_REFRESH_GRANT_TYPE = "refresh_token";
    private static final String DEVICE_ID = "DO_NOT_TRACK_THIS_DEVICE";
    private static final String OAUTH_BASE_URL = "https://www.reddit.com/api/v1/access_token";
    private static final String REDIRECT_URI = "nosurfforreddit://oauth";
    private static final String CLIENT_ID = "jPF59UF5MbMkWg";
    private static final String KEY_USER_OAUTH_ACCESS_TOKEN = "userAccessToken";
    private static final String KEY_USER_OAUTH_REFRESH_TOKEN = "userAccessRefreshToken";
    private static final String AUTH_HEADER = okhttp3.Credentials.basic(CLIENT_ID, "");
    private static final String APP_ONLY_AUTH_CALL_FAILED = "App-only auth call failed";
    private static final String USER_AUTH_CALL_FAILED = "User auth call failed";
    private static final String REFRESH_AUTH_CALL_FAILED = "Refresh auth call failed";
    private static final String FETCH_ALL_POSTS_CALL_FAILED = "fetchAllPostsASync call failed: ";
    private static final String FETCH_SUBSCRIBED_POSTS_CALL_FAILED = "fetchSubscribedPostsASync call failed: ";
    private static final String FETCH_POST_COMMENTS_CALL_FAILED = "fetchPostCommentsASync call failed: ";
    private static final String BEARER = "Bearer ";
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
    private static final int RESPONSE_CODE_401 = 401;

    // caches to let us keep working during asynchronous writes to SharedPrefs
    private String userOAuthAccessTokenCache = "";
    private String userOAuthRefreshTokenCache = "";
    private String appOnlyOAuthTokenCache = "";
    private boolean isUserLoggedInCache;

    // these 3 "raw" LiveData come straight from the Reddit API; only used internally in repo
    private final MutableLiveData<Listing> allPostsRawLiveData = new MutableLiveData<>();
    private final MutableLiveData<Listing> subscribedPostsRawLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> commentsRawLiveData = new MutableLiveData<>();

    // helper fields used to transform raw live LiveData into clean viewstates that feed the UI
    private final LiveData<List<ClickedPostId>> clickedPostIdsLiveData;
    private final PostsViewState mergedAllPostsCache = new PostsViewState();
    private final PostsViewState mergedSubscribedPostsCache = new PostsViewState();
    private String[] clickedPostIdsCache = new String[25];

    // these 3 "cleaned" LiveData feed the UI and have public getters
    private final LiveData<PostsViewState> allPostsViewStateLiveData;
    private final LiveData<PostsViewState> subscribedPostsViewStateLiveData;
    private final LiveData<CommentsViewState> commentsViewStateLiveData;

    // event feeds
    private final MutableLiveData<Boolean> isUserLoggedInLiveData = new MutableLiveData<>();

    private final RetrofitInterface ri;
    private final ClickedPostIdDao clickedPostIdDao;
    private final SharedPreferences preferences;
    private final InsertClickedPostIdThreadPoolExecutor executor;

    public NoSurfRepository(Retrofit retrofit,
                            SharedPreferences preferences,
                            ClickedPostIdRoomDatabase db,
                            InsertClickedPostIdThreadPoolExecutor executor) {
        this.preferences = preferences;
        ri = retrofit.create(RetrofitInterface.class);
        clickedPostIdDao = db.clickedPostIdDao();
        clickedPostIdsLiveData = clickedPostIdDao.getAllClickedPostIds();
        this.executor = executor;

        allPostsViewStateLiveData = mergeClickedPostIdsWithCleanedPostsRawLiveData(false);
        subscribedPostsViewStateLiveData = mergeClickedPostIdsWithCleanedPostsRawLiveData(true);
        commentsViewStateLiveData = cleanCommentsRawLiveData();
    }

    // region network auth calls -------------------------------------------------------------------

    /* Logged-out (aka anonymous, aka app-only users require an anonymous token to interact with
     * the Reddit API and view public posts and comments from r/all. This token is provided by
     * fetchAppOnlyOAuthTokenASync() and does not require any user credentials.
     *
     *  Also called to refresh this anonymous token when it expires */
    private void fetchAppOnlyOAuthTokenASync(final NetworkCallbacks callback, final String id) {
        ri.fetchAppOnlyOAuthTokenASync(
                OAUTH_BASE_URL,
                APP_ONLY_GRANT_TYPE,
                DEVICE_ID,
                AUTH_HEADER)
                .enqueue(new Callback<AppOnlyOAuthToken>() {

            @Override
            public void onResponse(Call<AppOnlyOAuthToken> call,
                                   Response<AppOnlyOAuthToken> response) {
                appOnlyOAuthTokenCache = response.body().getAccessToken();
                // don't bother saving this ephemeral token into sharedprefs

                switch (callback) {
                    case FETCH_ALL_POSTS_ASYNC:
                        fetchAllPostsASync();
                        break;
                    case FETCH_POST_COMMENTS_ASYNC:
                        fetchPostCommentsASync(id);
                        break;
                    case FETCH_SUBSCRIBED_POSTS_ASYNC:
                        // do nothing, as an app-only token is for logged-out users only
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Call<AppOnlyOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), APP_ONLY_AUTH_CALL_FAILED);
            }
        });
    }

    /* Logged-in users can view posts from their subscribed subreddits in addition to r/all,
     * but this requires a user OAuth token, which is provided by fetchUserOAuthTokenASync().
     *
     * Note that after the user is logged in, their user token is now also used for viewing
     * r/all, instead of the previously-fetched anonymous token from fetchAppOnlyOAuthTokenASync */
    public void fetchUserOAuthTokenASync(String code) {
        ri.fetchUserOAuthTokenASync(
                OAUTH_BASE_URL,
                USER_GRANT_TYPE,
                code,
                REDIRECT_URI,
                AUTH_HEADER)
                .enqueue(new Callback<UserOAuthToken>() {

            @Override
            public void onResponse(Call<UserOAuthToken> call,
                                   Response<UserOAuthToken> response) {
                userOAuthAccessTokenCache = response.body().getAccessToken();
                userOAuthRefreshTokenCache = response.body().getRefreshToken();

                preferences
                        .edit()
                        .putString(KEY_USER_OAUTH_ACCESS_TOKEN, userOAuthAccessTokenCache)
                        .putString(KEY_USER_OAUTH_REFRESH_TOKEN, userOAuthRefreshTokenCache)
                        .apply();

                setUserLoggedIn();
                fetchAllPostsASync();
                fetchSubscribedPostsASync();
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), USER_AUTH_CALL_FAILED);
            }
        });
    }

    private void refreshExpiredUserOAuthTokenASync(final NetworkCallbacks callback, final String id) {
        ri.refreshExpiredUserOAuthTokenASync(
                OAUTH_BASE_URL,
                USER_REFRESH_GRANT_TYPE,
                userOAuthRefreshTokenCache,
                AUTH_HEADER)
                .enqueue(new Callback<UserOAuthToken>() {

            @Override
            public void onResponse(Call<UserOAuthToken> call, Response<UserOAuthToken> response) {
                userOAuthAccessTokenCache = response.body().getAccessToken();

                preferences
                        .edit()
                        .putString(KEY_USER_OAUTH_ACCESS_TOKEN, userOAuthAccessTokenCache)
                        .apply();

                switch (callback) {
                    case FETCH_ALL_POSTS_ASYNC:
                        fetchAllPostsASync();
                        break;
                    case FETCH_SUBSCRIBED_POSTS_ASYNC:
                        fetchSubscribedPostsASync();
                        break;
                    case FETCH_POST_COMMENTS_ASYNC:
                        fetchPostCommentsASync(id);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), REFRESH_AUTH_CALL_FAILED);
            }
        });
    }

    // endregion network auth calls ----------------------------------------------------------------

    // region network data calls -------------------------------------------------------------------

    /* gets posts from r/all, using either a user or anonymous token based on the user's
       login status. Works for both logged-in and logged-out users */
    public void fetchAllPostsASync() {
        String accessToken;
        String bearerAuth;

        if (isUserLoggedInCache) {
            accessToken = userOAuthAccessTokenCache;
        } else {
            if (!"".equals(appOnlyOAuthTokenCache)) {
                accessToken = appOnlyOAuthTokenCache;
            } else {
                /* If user is logged out and there's no app only OAuth token in the cache,
                 * we need to fetch one.
                 *
                 * However, if a blank accessToken is sent to the server, we get back a 200 (OK)
                 * status code but with no data, which results in onFailure. To hack around this,
                 * we assign a garbage value to accessToken which ensures the below call gets a 401
                 * error instead of 200 and thus executes fetchAppOnlyOAuthTokenASync and its
                 * callback in the right order, instead of the call failing
                 *
                 * Dirty hack but simplifies the logic in onResponse */
                //TODO: fix this w/ RxJava
                accessToken = "xyz";
            }
        }

        bearerAuth = BEARER + accessToken;

        ri.fetchAllPostsASync(bearerAuth).enqueue(new Callback<Listing>() {

            /* conditional logic here fetches or refreshes expired tokens if there's a 401
             * error, and passes itself as a callback to try fetching posts once again after the
             * token has been refreshed
             *
             * I use callbacks this way to "react" to expired tokens instead of running some
             * background timer task that refreshes them every X minutes */
            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {
                if ((response.code() == RESPONSE_CODE_401) && (isUserLoggedInCache)) {
                    refreshExpiredUserOAuthTokenASync(NetworkCallbacks.FETCH_ALL_POSTS_ASYNC, "");
                } else if (response.code() == RESPONSE_CODE_401) {
                    fetchAppOnlyOAuthTokenASync(NetworkCallbacks.FETCH_ALL_POSTS_ASYNC, "");
                } else {
                    allPostsRawLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.e(getClass().toString(), FETCH_ALL_POSTS_CALL_FAILED + t.toString());
            }
        });
    }

    /* gets posts from the user's subscribed subreddits; only applicable to logged-in users */
    public void fetchSubscribedPostsASync() {
        String bearerAuth = BEARER + userOAuthAccessTokenCache;

        //noinspection StatementWithEmptyBody
        if (isUserLoggedInCache) {
            ri.fetchSubscribedPostsASync(bearerAuth).enqueue(new Callback<Listing>() {

                // same callback logic as documented in fetchAllPostsASync()
                @Override
                public void onResponse(Call<Listing> call, Response<Listing> response) {
                    if (response.code() == RESPONSE_CODE_401) {
                        refreshExpiredUserOAuthTokenASync(NetworkCallbacks.FETCH_SUBSCRIBED_POSTS_ASYNC, "");
                    } else {
                        subscribedPostsRawLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<Listing> call, Throwable t) {
                    Log.e(getClass().toString(), FETCH_SUBSCRIBED_POSTS_CALL_FAILED + t.toString());
                }
            });
        } else {
            // do nothing if user is logged out, as subscribed posts are only for logged-in users
        }
    }

    /* get a post's comments; works for both logged-in and logged-out users */
    public void fetchPostCommentsASync(final String id) {

        //noinspection StatementWithEmptyBody
        if (!"".equals(id)) {
            String accessToken;
            String bearerAuth;

            if (isUserLoggedInCache) {
                accessToken = userOAuthAccessTokenCache;
            } else {
                accessToken = appOnlyOAuthTokenCache;
            }

            bearerAuth = BEARER + accessToken;

            ri.fetchPostCommentsASync(bearerAuth, id).enqueue(new Callback<List<Listing>>() {

                // same callback logic as documented in fetchAllPostsASync()
                @Override
                public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                    if ((response.code() == RESPONSE_CODE_401) && (isUserLoggedInCache)) {
                        refreshExpiredUserOAuthTokenASync(NetworkCallbacks.FETCH_POST_COMMENTS_ASYNC, id);
                    } else if (response.code() == RESPONSE_CODE_401) {
                        fetchAppOnlyOAuthTokenASync(NetworkCallbacks.FETCH_POST_COMMENTS_ASYNC, id);
                    } else {
                        commentsRawLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<Listing>> call, Throwable t) {
                    Log.e(getClass().toString(), FETCH_POST_COMMENTS_CALL_FAILED + t.toString());
                }
            });
        } else {
            // do nothing if blank id is passed
        }
    }

    // endregion network data calls ----------------------------------------------------------------

    // region init/de-init methods -----------------------------------------------------------------

    /* login credentials are stored in SharedPreferences to survive not only config changes,
     *  but process termination and reboots, so user doesn't have to re-login every time the app
     *  exits
     *
     *  This method should run on app initialization, to see if the user's credentials have been
     *  previously saved */
    public void checkIfLoginCredentialsAlreadyExist() {
        userOAuthAccessTokenCache = preferences
                .getString(KEY_USER_OAUTH_ACCESS_TOKEN, "");

        userOAuthRefreshTokenCache = preferences
                .getString(KEY_USER_OAUTH_REFRESH_TOKEN, "");

        if (!"".equals(userOAuthAccessTokenCache) && !"".equals(userOAuthRefreshTokenCache)) {
            setUserLoggedIn();
        } else {
            // need to explicitly call this here to help ContainerFragment set itself up
            setUserLoggedOut();
        }
    }

    private void setUserLoggedIn() {
        isUserLoggedInCache = true;
        isUserLoggedInLiveData.setValue(true);
    }

    public void setUserLoggedOut() {
        isUserLoggedInCache = false;
        isUserLoggedInLiveData.setValue(false);

        userOAuthAccessTokenCache = "";
        userOAuthRefreshTokenCache = "";

        preferences
                .edit()
                .putString(KEY_USER_OAUTH_ACCESS_TOKEN, "")
                .putString(KEY_USER_OAUTH_REFRESH_TOKEN, "")
                .apply();
    }

    // endregion init/de-init methods --------------------------------------------------------------

    // region viewstate Transformations ------------------------------------------------------------

    private LiveData<CommentsViewState> cleanCommentsRawLiveData() {
        return Transformations.map(commentsRawLiveData, input -> {
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

            commentsViewState.id = getCommentId(input);

            return commentsViewState;
        });
    }

    /* Cleans dirty/raw post data from the Reddit API
     *
     * Note that this is only "stage 1" - the resulting object is not ready for the UI.
     * Instead the result here is piped into mergeClickedPostIdsWithCleanedPostsRawLiveData, which
     * is"stage 2" and creates a UI-ready object that knows which posts have already been
     *  clicked */
    private LiveData<PostsViewState> cleanPostsRawLiveData(boolean isSubscribedPosts) {
        LiveData<Listing> postsLiveData;

        if (isSubscribedPosts) {
            postsLiveData = subscribedPostsRawLiveData;
        } else {
            postsLiveData = allPostsRawLiveData;
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

    /* "Stage 2" of viewstate preparation, in which cleaned post data returned by
     * cleanPostsRawLiveData is merged into a new object that also knows which posts have
     * been clicked (accomplished by checking post IDs against post IDs that have already
     * been written into the Room database */
    private LiveData<PostsViewState> mergeClickedPostIdsWithCleanedPostsRawLiveData(boolean isSubscribedPosts){
        final MediatorLiveData<PostsViewState> mediator = new MediatorLiveData<>();

        LiveData<PostsViewState> postsLiveDataViewState;
        PostsViewState postsViewStateCache;

        if (isSubscribedPosts) {
            postsLiveDataViewState = cleanPostsRawLiveData(true);
            postsViewStateCache = mergedSubscribedPostsCache;
        } else {
            postsLiveDataViewState = cleanPostsRawLiveData(false);
            postsViewStateCache = mergedAllPostsCache;
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

    /* These are mostly data cleaning routines that get applied against the raw data from
     * the Reddit API */

    private void updateCachedClickedPostIds(PostsViewState postsViewStateCache, int i) {
        if (Arrays.asList(clickedPostIdsCache).contains(postsViewStateCache.postData.get(i).id)) {
            postsViewStateCache.hasBeenClicked[i] = true;
        }
    }

    // Reddit API provides twice-encoded HTML... ¯\_(ツ)_/¯
    private String formatSelfPostSelfTextHtml(String twiceEncodedSelfTextHtml) {
        if ((twiceEncodedSelfTextHtml != null) && (!"".equals(twiceEncodedSelfTextHtml))) {
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

    private String getCommentId(List<Listing> input) {
        return input
                .get(0)
                .getData()
                .getChildren()
                .get(0)
                .getData()
                .getId();
    }

    private String formatCommentDetails(String commentAuthor, int commentScore) {
        return USER_ABBREVIATION
                + commentAuthor
                + BULLET_POINT
                + Integer.toString(commentScore);
    }

    private String getCommentBodyHtml(List<Listing> input, int autoModOffset, int i) {
        Data_ data = input.get(1)
                .getData()
                .getChildren()
                .get(autoModOffset + i)
                .getData();

        return decodeHtml(data.getBodyHtml()).toString();
    }

    private Spanned decodeHtml(String encoded) {
        return HtmlCompat.fromHtml(encoded, HtmlCompat.FROM_HTML_MODE_LEGACY);
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

    // region event handling -----------------------------------------------------------------------

    // EMPTY

    //endregion event handling ---------------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return isUserLoggedInLiveData;
    }

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return allPostsViewStateLiveData;
    }

    public LiveData<PostsViewState> getSubscribedPostsViewStateLiveData() {
        return subscribedPostsViewStateLiveData;
    }

    public LiveData<CommentsViewState> getCommentsViewStateLiveData() {
        return commentsViewStateLiveData;
    }

    // endregion getter methods --------------------------------------------------------------------

    // region room methods and classes -------------------------------------------------------------

    public void insertClickedPostId(ClickedPostId id) {
        executor.insertClickedPostId(clickedPostIdDao, id);
    }

    // returns the list of clicked post IDs stored in the Room database
    private LiveData<String[]> getClickedPostIdsLiveData() {
        return Transformations.map(clickedPostIdsLiveData, input -> {
            int size = input.size();

            String[] clickedPostIds = new String[size];

            for (int i = 0; i < size; i++) {
                clickedPostIds[i] = input.get(i).getClickedPostId();
            }

            return clickedPostIds;
        });
    }

    // endregion room methods and classes ----------------------------------------------------------

    // region enums --------------------------------------------------------------------------------

    private enum NetworkCallbacks {
        FETCH_ALL_POSTS_ASYNC,
        FETCH_POST_COMMENTS_ASYNC,
        FETCH_SUBSCRIBED_POSTS_ASYNC
    }

    // endregion enums -----------------------------------------------------------------------------
}
