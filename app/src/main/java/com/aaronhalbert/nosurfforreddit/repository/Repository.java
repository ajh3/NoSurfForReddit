package com.aaronhalbert.nosurfforreddit.repository;

import android.util.Log;

import com.aaronhalbert.nosurfforreddit.Event;
import com.aaronhalbert.nosurfforreddit.repository.redditschema.Data_;
import com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostIdDao;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostIdRoomDatabase;
import com.aaronhalbert.nosurfforreddit.viewstate.CommentsViewState;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.aaronhalbert.nosurfforreddit.repository.Repository.NetworkErrors.FETCH_ALL_POSTS_ERROR;
import static com.aaronhalbert.nosurfforreddit.repository.Repository.NetworkErrors.FETCH_POST_COMMENTS_ERROR;
import static com.aaronhalbert.nosurfforreddit.repository.Repository.NetworkErrors.FETCH_SUBSCRIBED_POSTS_ERROR;

public class Repository {
    private static final String FETCH_ALL_POSTS_CALL_FAILED = "fetchAllPostsASync call failed: ";
    private static final String FETCH_SUBSCRIBED_POSTS_CALL_FAILED = "fetchSubscribedPostsASync call failed: ";
    private static final String FETCH_POST_COMMENTS_CALL_FAILED = "fetchPostCommentsASync call failed: ";
    private static final String BEARER = "Bearer ";
    private static final String ZERO = "zero";
    private static final int RESPONSE_CODE_401 = 401;

    // these 3 "raw" LiveData come straight from the Reddit API; only used internally in repo
    private final MutableLiveData<Listing> allPostsRawLiveData = new MutableLiveData<>();
    private final MutableLiveData<Listing> subscribedPostsRawLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> commentsRawLiveData = new MutableLiveData<>();

    // helper fields used to clean raw LiveData; only used internally in repo
    private final LiveData<List<ClickedPostId>> clickedPostIdsLiveData;
    private final PostsViewState mergedAllPostsCache = new PostsViewState();
    private final PostsViewState mergedSubscribedPostsCache = new PostsViewState();
    private String[] clickedPostIdsCache = new String[25];

    // these 3 "cleaned" LiveData feed the UI directly and have public getters
    private final LiveData<PostsViewState> allPostsViewStateLiveData;
    private final LiveData<PostsViewState> subscribedPostsViewStateLiveData;
    private final LiveData<CommentsViewState> commentsViewStateLiveData;

    // user login status
    private final LiveData<Boolean> isUserLoggedInLiveData;

    // event feeds
    private final MutableLiveData<Event<NetworkErrors>> networkErrorsLiveData = new MutableLiveData<>();

    // other
    private final RetrofitContentInterface ri;
    private final ClickedPostIdDao clickedPostIdDao;
    private final ExecutorService executor;
    private final NoSurfAuthenticator authenticator;

    public Repository(Retrofit retrofit,
                      ClickedPostIdRoomDatabase db,
                      ExecutorService executor,
                      NoSurfAuthenticator authenticator) {
        authenticator.setRepository(this);
        this.executor = executor;
        this.authenticator = authenticator;
        isUserLoggedInLiveData = authenticator.isUserLoggedInLiveData;
        ri = retrofit.create(RetrofitContentInterface.class);
        clickedPostIdDao = db.clickedPostIdDao();
        clickedPostIdsLiveData = clickedPostIdDao.getAllClickedPostIds();
        allPostsViewStateLiveData = mergeClickedPostIdsWithCleanedPostsRawLiveData(false);
        subscribedPostsViewStateLiveData = mergeClickedPostIdsWithCleanedPostsRawLiveData(true);
        commentsViewStateLiveData = cleanCommentsRawLiveData();

        // initialize self
        checkIfLoginCredentialsAlreadyExist();
        fetchAllPostsASync();
        fetchSubscribedPostsASync();
    }

    // region network auth calls -------------------------------------------------------------------

    public void fetchUserOAuthTokenASync(String code) {
        authenticator.fetchUserOAuthTokenASync(code);
    }

    // endregion network auth calls ----------------------------------------------------------------

    // region network data calls -------------------------------------------------------------------

    /* gets posts from r/all, using either a user or anonymous token based on the user's
       login status. Works for both logged-in and logged-out users */
    public void fetchAllPostsASync() {
        String accessToken;
        String bearerAuth;

        if (authenticator.isUserLoggedInCache()) {
            accessToken = authenticator.getUserOAuthAccessTokenCache();
        } else {
            if (!"".equals(authenticator.getAppOnlyOAuthTokenCache())) {
                accessToken = authenticator.getAppOnlyOAuthTokenCache();
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
                if ((response.code() == RESPONSE_CODE_401) && (authenticator.isUserLoggedInCache())) {
                    authenticator.refreshExpiredUserOAuthTokenASync(NetworkCallbacks.FETCH_ALL_POSTS_ASYNC, "");
                } else if (response.code() == RESPONSE_CODE_401) {
                    authenticator.fetchAppOnlyOAuthTokenASync(NetworkCallbacks.FETCH_ALL_POSTS_ASYNC, "");
                } else {
                    allPostsRawLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.e(getClass().toString(), FETCH_ALL_POSTS_CALL_FAILED, t);
                setNetworkErrorsLiveData(new Event<>(FETCH_ALL_POSTS_ERROR));
            }
        });
    }

    /* gets posts from the user's subscribed subreddits; only applicable to logged-in users */
    public void fetchSubscribedPostsASync() {
        String bearerAuth = BEARER + authenticator.getUserOAuthAccessTokenCache();

        //noinspection StatementWithEmptyBody
        if (authenticator.isUserLoggedInCache()) {
            ri.fetchSubscribedPostsASync(bearerAuth).enqueue(new Callback<Listing>() {

                // same callback logic as documented in fetchAllPostsASync()
                @Override
                public void onResponse(Call<Listing> call, Response<Listing> response) {
                    if (response.code() == RESPONSE_CODE_401) {
                        authenticator.refreshExpiredUserOAuthTokenASync(NetworkCallbacks.FETCH_SUBSCRIBED_POSTS_ASYNC, "");
                    } else {
                        subscribedPostsRawLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<Listing> call, Throwable t) {
                    Log.e(getClass().toString(), FETCH_SUBSCRIBED_POSTS_CALL_FAILED, t);
                    setNetworkErrorsLiveData(new Event<>(FETCH_SUBSCRIBED_POSTS_ERROR));
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

            if (authenticator.isUserLoggedInCache()) {
                accessToken = authenticator.getUserOAuthAccessTokenCache();
            } else {
                accessToken = authenticator.getAppOnlyOAuthTokenCache();
            }

            bearerAuth = BEARER + accessToken;

            ri.fetchPostCommentsASync(bearerAuth, id).enqueue(new Callback<List<Listing>>() {

                // same callback logic as documented in fetchAllPostsASync()
                @Override
                public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                    if ((response.code() == RESPONSE_CODE_401) && (authenticator.isUserLoggedInCache())) {
                        authenticator.refreshExpiredUserOAuthTokenASync(NetworkCallbacks.FETCH_POST_COMMENTS_ASYNC, id);
                    } else if (response.code() == RESPONSE_CODE_401) {
                        authenticator.fetchAppOnlyOAuthTokenASync(NetworkCallbacks.FETCH_POST_COMMENTS_ASYNC, id);
                    } else {
                        commentsRawLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<Listing>> call, Throwable t) {
                    Log.e(getClass().toString(), FETCH_POST_COMMENTS_CALL_FAILED, t);
                    setNetworkErrorsLiveData(new Event<>(FETCH_POST_COMMENTS_ERROR));
                }
            });
        } else {
            // do nothing if blank id is passed
        }
    }

    // endregion network data calls ----------------------------------------------------------------

    // region init/de-init methods -----------------------------------------------------------------

    public void checkIfLoginCredentialsAlreadyExist() {
        authenticator.checkIfLoginCredentialsAlreadyExist();
    }

    void setUserLoggedIn() {
        authenticator.setUserLoggedIn();
    }

    public void setUserLoggedOut() {
        authenticator.setUserLoggedOut();
    }

    // endregion init/de-init methods --------------------------------------------------------------

    // region viewstate Transformations ------------------------------------------------------------

    private LiveData<CommentsViewState> cleanCommentsRawLiveData() {
        return Transformations.map(commentsRawLiveData, input -> {
            CommentsViewState commentsViewState;
            int autoModOffset;

            //check if there is at least 1 comment
            if (input.get(1).getNumTopLevelComments() > 0) {

                //calculate the number of valid comments after checking for & excluding AutoMod
                autoModOffset = input.get(1).calculateAutoModOffset();
                int numComments = input.get(1).getNumTopLevelComments() - autoModOffset;

                // only display first 3 top-level comments
                if (numComments > 3) numComments = 3;

                commentsViewState = new CommentsViewState(numComments, input.get(0).getCommentId());

                // construct the viewstate object
                for (int i = 0; i < numComments; i++) {
                    String commentAuthor = input.get(1).getCommentAuthor(autoModOffset + i);
                    int commentScore = input.get(1).getCommentScore(autoModOffset, i);

                    commentsViewState.commentBodies[i] = input.get(1).formatCommentBodyHtml(autoModOffset, i);
                    commentsViewState.commentDetails[i] = input.get(0).formatCommentDetails(commentAuthor, commentScore);
                }
            } else { //if zero comments
                commentsViewState = new CommentsViewState(0, ZERO);
            }

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
                postDatum.title = input.decodeHtml(data.getTitle()).toString(); // some titles contain HTML special entities
                postDatum.author = data.getAuthor();
                postDatum.subreddit = data.getSubreddit();
                postDatum.score = data.getScore();
                postDatum.numComments = data.getNumComments();
                postDatum.thumbnailUrl = input.pickThumbnailUrl(data.getThumbnail());
                postDatum.isNsfw = data.isNsfw();

                // assign link- and self-post specific attributes
                if (postDatum.isSelf) {
                    postDatum.selfTextHtml = input.formatSelfPostSelfTextHtml(data.getSelfTextHtml());
                } else {
                    postDatum.url = input.decodeHtml(data.getUrl()).toString();
                    postDatum.imageUrl = input.decodeHtml(input.pickImageUrl(i)).toString();
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

        LiveData<PostsViewState> postsViewStateLiveData;
        PostsViewState postsViewStateCache;

        if (isSubscribedPosts) {
            postsViewStateLiveData = cleanPostsRawLiveData(true);
            postsViewStateCache = mergedSubscribedPostsCache;
        } else {
            postsViewStateLiveData = cleanPostsRawLiveData(false);
            postsViewStateCache = mergedAllPostsCache;
        }

        /* whenever a new PostsViewState object arrives, cache it, update the cache with
         * the latest list of clicked post IDs, and emit the result. Note that when a new
         * PostsViewState object arrives, it is because the list of posts in the app has
         * been reloaded. Thus, we emit a result every time the list is reloaded, and it
         * always is checked against the most recent list of clicked post IDs, which
         * is kept up to date by the other source LiveData. */
        mediator.addSource(postsViewStateLiveData, postsViewState -> {
            for (int i = 0; i < 25; i++) {
                postsViewStateCache.postData.set(i, postsViewState.postData.get(i));

                updatePostsViewStateCacheWithLatestClickedPostIds(postsViewStateCache, i);
            }

            mediator.setValue(postsViewStateCache);
        });

        /* whenever a new list of clicked post IDs arrives, cache it and update the latest
         * PostsViewState cache, so it is ready to be emitted the next time a PostsViewState
         * object arrives to the other source LiveData. Here we do NOT set a result on the
         * mediator, as we never want to emit a result that has clicked post IDs but no
         * PostsViewState. In other words, whatever arrives here is prepared and stashed in
         * postsViewStateCache, and there it sits until the next PostsViewState arrives to the
         * other source LiveData, and then the whole bundle is emitted. */
        mediator.addSource(getClickedPostIdsLiveData(), strings -> {
            clickedPostIdsCache = strings;

            for (int i = 0; i < 25; i++) {
                updatePostsViewStateCacheWithLatestClickedPostIds(postsViewStateCache, i);
            }
        });

        return mediator;
    }

    // endregion viewstate Transformations ---------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void updatePostsViewStateCacheWithLatestClickedPostIds(PostsViewState postsViewStateCache, int i) {
        postsViewStateCache.hasBeenClicked[i] =
                Arrays.asList(clickedPostIdsCache).contains(postsViewStateCache.postData.get(i).id);
    }

    // endregion helper methods --------------------------------------------------------------------

    // region event handling -----------------------------------------------------------------------

    void setNetworkErrorsLiveData(Event<NetworkErrors> n) {
        networkErrorsLiveData.setValue(n);
    }

    public LiveData<Event<NetworkErrors>> getNetworkErrorsLiveData() {
        return networkErrorsLiveData;
    }

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
        Runnable runnable = () -> clickedPostIdDao.insertClickedPostId(id);

        executor.execute(runnable);
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

    enum NetworkCallbacks {
        FETCH_ALL_POSTS_ASYNC,
        FETCH_POST_COMMENTS_ASYNC,
        FETCH_SUBSCRIBED_POSTS_ASYNC
    }

    public enum NetworkErrors {
        FETCH_ALL_POSTS_ERROR,
        FETCH_POST_COMMENTS_ERROR,
        FETCH_SUBSCRIBED_POSTS_ERROR,
        APP_ONLY_AUTH_CALL_ERROR,
        USER_AUTH_CALL_ERROR,
        REFRESH_AUTH_CALL_ERROR
    }

    // endregion enums -----------------------------------------------------------------------------
}
