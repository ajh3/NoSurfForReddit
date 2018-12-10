package com.aaronhalbert.nosurfforreddit.repository;

import android.annotation.SuppressLint;
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
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
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

    // these 3 "cleaned" LiveData feed the UI directly and have public getters
    private final MutableLiveData<PostsViewState> allPostsViewStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<PostsViewState> subscribedPostsViewStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<CommentsViewState> commentsViewStateLiveData = new MutableLiveData<>();

    // user login status
    private final LiveData<Boolean> isUserLoggedInLiveData;

    // event feeds
    private final MutableLiveData<Event<NetworkErrors>> networkErrorsLiveData = new MutableLiveData<>();

    // other
    private final RetrofitContentInterface ri;
    private final ClickedPostIdDao clickedPostIdDao;
    private final ExecutorService executor;
    private final NoSurfAuthenticator authenticator;



    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void fetchAllPostsASync() {
        String bearerAuth = selectAllPostsAndCommentsBearerAuth();

        //TODO: update these comments for RxJava implmentation
        /* conditional logic here fetches or refreshes expired tokens if there's a 401
         * error, and passes itself as a callback to try fetching posts once again after the
         * token has been refreshed
         *
         * I use callbacks this way to "react" to expired tokens instead of running some
         * background timer task that refreshes them every X minutes */

        Maybe<PostsViewState> call = ri.fetchAllPostsASync(bearerAuth)
                .doOnError(throwable -> Log.d(getClass().toString(), "retrying FETCH_ALL_POSTS_CALL", throwable))
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof HttpException) {
                        if (((HttpException) throwable).code() == 401 && (authenticator.isUserLoggedInCache())) {
                            return authenticator.refreshExpiredUserOAuthTokenASync()
                                    .flatMap(userOAuthToken -> retryFetchAllPostsASync());
                        } else if (((HttpException) throwable).code() == 401) {
                            return authenticator.fetchAppOnlyOAuthTokenASync()
                                    .flatMap(appOnlyOAuthToken -> retryFetchAllPostsASync());
                        }
                    }
                    return Maybe.error(throwable);
                })
                .observeOn(Schedulers.computation())
                .map(this::cleanRawPosts);

        Flowable.combineLatest(call.toFlowable(), fetchClickedPostIds(), setClickedPosts)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allPostsViewStateLiveData::setValue,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(FETCH_ALL_POSTS_ERROR));
                            Log.d(getClass().toString(), FETCH_ALL_POSTS_CALL_FAILED, throwable);
                        });
    }



    /* gets posts from the user's subscribed subreddits; only applicable to logged-in users */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void fetchSubscribedPostsASync() {
        if (authenticator.isUserLoggedInCache()) {
            String bearerAuth = selectSubscribedPostsBearerAuth();

            Maybe<PostsViewState> call = ri.fetchSubscribedPostsASync(bearerAuth)
                    .doOnError(throwable -> Log.d(getClass().toString(), "retrying FETCH_SUBSCRIBED_POSTS_CALL", throwable))
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof HttpException) {
                            if (((HttpException) throwable).code() == 401) {
                                return authenticator.refreshExpiredUserOAuthTokenASync()
                                        .flatMap(userOAuthToken -> retryFetchSubscribedPostsASync());
                            }
                        }
                        return Maybe.error(throwable);
                    })
                    .observeOn(Schedulers.computation())
                    .map(this::cleanRawPosts);

            Flowable.combineLatest(call.toFlowable(), fetchClickedPostIds(), setClickedPosts)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            subscribedPostsViewStateLiveData::setValue,
                            throwable -> {
                                setNetworkErrorsLiveData(new Event<>(FETCH_SUBSCRIBED_POSTS_ERROR));
                                Log.d(getClass().toString(), FETCH_SUBSCRIBED_POSTS_CALL_FAILED, throwable);
                            });

        } // do nothing if user is logged out, as subscribed posts are only for logged-in users
    }






    /* get a post's comments; works for both logged-in and logged-out users */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void fetchPostCommentsASync(final String id) {
        if (!"".equals(id)) { // do nothing if blank id is passed
            String bearerAuth = selectAllPostsAndCommentsBearerAuth();

            ri.fetchPostCommentsASync(bearerAuth, id)
                    .doOnError(throwable -> Log.d(getClass().toString(), "retrying FETCH_POST_COMMENTS_CALL", throwable))
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof HttpException) {
                            if (((HttpException) throwable).code() == 401 && (authenticator.isUserLoggedInCache())) {
                                return authenticator.refreshExpiredUserOAuthTokenASync()
                                        .flatMap(userOAuthToken -> retryFetchPostCommentsASync(id));
                            } else if (((HttpException) throwable).code() == 401) {
                                return authenticator.fetchAppOnlyOAuthTokenASync()
                                        .flatMap(appOnlyOAuthToken -> retryFetchPostCommentsASync(id));
                            }
                        }
                        return Maybe.error(throwable);
                    })
                    .observeOn(Schedulers.computation())
                    .map(this::cleanRawComments)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            commentsViewStateLiveData::setValue,
                            throwable -> {
                                setNetworkErrorsLiveData(new Event<>(FETCH_POST_COMMENTS_ERROR));
                                Log.d(getClass().toString(), FETCH_POST_COMMENTS_CALL_FAILED, throwable);
                            });

        }
    }

    private MaybeSource<? extends Listing> retryFetchAllPostsASync() {
        String bearerAuth = selectAllPostsAndCommentsBearerAuth();
        return ri.fetchAllPostsASync(bearerAuth);
    }

    private MaybeSource<? extends Listing> retryFetchSubscribedPostsASync() {
        String bearerAuth = selectSubscribedPostsBearerAuth();
        return ri.fetchSubscribedPostsASync(bearerAuth);
    }

    private MaybeSource<? extends List<Listing>> retryFetchPostCommentsASync(String id) {
        String bearerAuth = selectAllPostsAndCommentsBearerAuth();
        return ri.fetchPostCommentsASync(bearerAuth, id);
    }


    private String selectAllPostsAndCommentsBearerAuth() {
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
        return bearerAuth;
    }



    private String selectSubscribedPostsBearerAuth() {
        return BEARER + authenticator.getUserOAuthAccessTokenCache();
    }



    private final BiFunction<PostsViewState, String[], PostsViewState> setClickedPosts
            = (postsViewState, ids) -> {
        List list = Arrays.asList(ids);

        for (int i = 0; i < 25; i++) {
            if (list.contains(postsViewState.postData.get(i).id)) {
                postsViewState.hasBeenClicked[i] = true;
            }
        }

        return postsViewState;
    };

    // endregion network data calls ----------------------------------------------------------------



    // region viewstate Transformations ------------------------------------------------------------



    /* Cleans dirty/raw post data from the Reddit API
     *
     * Note that this is only "stage 1" - the resulting object is not ready for the UI.
     * Instead the result here is piped into mergeClickedPostIdsWithCleanedPostsRawLiveData, which
     * is"stage 2" and creates a UI-ready object that knows which posts have already been
     *  clicked */
    private PostsViewState cleanRawPosts(Listing input) {

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
            postDatum.permalink = data.getPermalink();

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
    }

    //TODO rename
    private CommentsViewState cleanRawComments(List<Listing> input) {
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
    }



    /* "Stage 2" of viewstate preparation, in which cleaned post data returned by
     * cleanPostsRawLiveData is merged into a new object that also knows which posts have
     * been clicked (accomplished by checking post IDs against post IDs that have already
     * been written into the Room database */


    // endregion viewstate Transformations ---------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------



    // endregion helper methods --------------------------------------------------------------------



    // region room methods and classes -------------------------------------------------------------

    public void insertClickedPostId(ClickedPostId id) {
        Runnable runnable = () -> clickedPostIdDao.insertClickedPostId(id);

        executor.execute(runnable);
    }




    private Flowable<String[]> fetchClickedPostIds() {
        return clickedPostIdDao
                .getAllClickedPostIds()
                .map(this::getArrayOfClickedPostIds);
    }

    // returns the list of clicked post IDs stored in the Room database
    private String[] getArrayOfClickedPostIds(List<ClickedPostId> input) {

        int size = input.size();

        String[] clickedPostIds = new String[size];

        for (int i = 0; i < size; i++) {
            clickedPostIds[i] = input.get(i).getClickedPostId();
        }

        return clickedPostIds;
    }

    // endregion room methods and classes ----------------------------------------------------------

    // region init/de-init methods -----------------------------------------------------------------

    private void checkIfLoginCredentialsAlreadyExist() {
        authenticator.checkIfLoginCredentialsAlreadyExist();
    }

    void setUserLoggedIn() {
        authenticator.setUserLoggedIn();
    }

    public void setUserLoggedOut() {
        authenticator.setUserLoggedOut();
    }

    // endregion init/de-init methods --------------------------------------------------------------

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

    // region enums --------------------------------------------------------------------------------

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
