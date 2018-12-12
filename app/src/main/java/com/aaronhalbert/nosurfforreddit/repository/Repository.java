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
import io.reactivex.Observable;
import io.reactivex.Single;
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

    /* these MutableLiveData are exposed as LiveData through public getters, and feed the UI
     * directly. Alternatively, we could remove all LiveData from the repository, instead
     * exposing only Rx observables and make the switchover to LiveData in the ViewModel using
     * LiveDataReactiveStreams. However, most of the Rx work being done in the repository is
     * Single calls which end automatically by themselves, and so there is no real benefit
     * to getting lifecycle-aware unsubscription from LiveDataReactiveStreams. Instead, we just
     * have the observers write to these MutableLiveData directly in the repository. */
    private final MutableLiveData<PostsViewState> allPostsViewStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<PostsViewState> subscribedPostsViewStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<CommentsViewState> commentsViewStateLiveData = new MutableLiveData<>();

    private final LiveData<Boolean> isUserLoggedInLiveData;
    private final MutableLiveData<Event<NetworkErrors>> networkErrorsLiveData = new MutableLiveData<>();

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

    /* gets posts from r/all, with either a user (logged-in) or anonymous (logged-out, aka
     * "app-only") token based on the user's login status. Works for both logged-in and logged-out
     * users. */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void fetchAllPostsASync() {
        String bearerAuth = createAllPostsAndCommentsBearerAuth();

        /* fetches or refreshes expired tokens when there's a 401 error, and retries the request
         * after the token has been refreshed.
         *
         * We "react" to expired tokens dynamically instead of calculating the date/time of token
         * expiration and trying to time refreshes pre-emptively. */
        Single<PostsViewState> call = ri.fetchAllPostsASync(bearerAuth)
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof HttpException) {
                        if (((HttpException) throwable).code() == 401 && (authenticator.isUserLoggedInCache())) {
                            return authenticator.refreshExpiredUserOAuthTokenASync()
                                    .flatMap(userOAuthToken ->
                                            ri.fetchAllPostsASync(createAllPostsAndCommentsBearerAuth()));
                        } else if (((HttpException) throwable).code() == 401) {
                            return authenticator.fetchAppOnlyOAuthTokenASync()
                                    .flatMap(appOnlyOAuthToken ->
                                            ri.fetchAllPostsASync(createAllPostsAndCommentsBearerAuth()));
                        }
                    }
                    return Single.error(throwable);
                })
                .observeOn(Schedulers.computation())
                .map(this::cleanRawPosts);

        /* combines the results of the above call with the latest list of clicked post IDs from
         * the Room database, so the UI knows which posts to gray/X-out. The list of posts is a
         * Observable, so we have to convert the Single network call to a Observable as well. */
        Observable.combineLatest(call.toObservable(), fetchClickedPostIds(), mergeClickedPosts)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allPostsViewStateLiveData::setValue,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(FETCH_ALL_POSTS_ERROR));
                            Log.d(getClass().toString(), FETCH_ALL_POSTS_CALL_FAILED, throwable);
                        });
    }

    /* gets posts from the user's subscribed subreddits; only works for logged-in users. See
     * fetchAllPostsASync() for more comments that also apply to this method. */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void fetchSubscribedPostsASync() {
        if (authenticator.isUserLoggedInCache()) {
            String bearerAuth = createSubscribedPostsBearerAuth();

            Single<PostsViewState> call = ri.fetchSubscribedPostsASync(bearerAuth)
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof HttpException) {
                            if (((HttpException) throwable).code() == 401) {
                                return authenticator.refreshExpiredUserOAuthTokenASync()
                                        .flatMap(userOAuthToken ->
                                                ri.fetchSubscribedPostsASync(createSubscribedPostsBearerAuth()));
                            }
                        }
                        return Single.error(throwable);
                    })
                    .observeOn(Schedulers.computation())
                    .map(this::cleanRawPosts);

            Observable.combineLatest(call.toObservable(), fetchClickedPostIds(), mergeClickedPosts)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            subscribedPostsViewStateLiveData::setValue,
                            throwable -> {
                                setNetworkErrorsLiveData(new Event<>(FETCH_SUBSCRIBED_POSTS_ERROR));
                                Log.d(getClass().toString(), FETCH_SUBSCRIBED_POSTS_CALL_FAILED, throwable);
                            });
        }

        // do nothing if user is logged out, as subscribed posts are only for logged-in users
    }

    /* get a post's comments. This is called every time a user clicks a post, and works for both
     * logged-in and logged-out users. Works the same regardless of whether the post is a
     * link post or a self post. */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void fetchPostCommentsASync(final String id) {
        if (!"".equals(id)) {
            String bearerAuth = createAllPostsAndCommentsBearerAuth();

            ri.fetchPostCommentsASync(bearerAuth, id)
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof HttpException) {
                            if (((HttpException) throwable).code() == 401 && (authenticator.isUserLoggedInCache())) {
                                return authenticator.refreshExpiredUserOAuthTokenASync()
                                        .flatMap(userOAuthToken ->
                                                ri.fetchPostCommentsASync(createAllPostsAndCommentsBearerAuth(), id));
                            } else if (((HttpException) throwable).code() == 401) {
                                return authenticator.fetchAppOnlyOAuthTokenASync()
                                        .flatMap(appOnlyOAuthToken ->
                                                ri.fetchPostCommentsASync(createAllPostsAndCommentsBearerAuth(), id));
                            }
                        }
                        return Single.error(throwable);
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

    // endregion network data calls ----------------------------------------------------------------

    // region viewstate Transformations ------------------------------------------------------------

    /* Cleans dirty/raw post data from the Reddit API. Prepares it to be merged with the list
     * of clicked post IDs from the Room database, before being passed to the UI. */
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

    /* Cleans dirty/raw comment data from the Reddit API. */
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

    // endregion viewstate Transformations ---------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    /* used by fetchAllPostsASync() and fetchPostCommentsASync() - they both take the same
     * auth token. */
    private String createAllPostsAndCommentsBearerAuth() {
        String accessToken;

        if (authenticator.isUserLoggedInCache()) {
            accessToken = authenticator.getUserOAuthAccessTokenCache();
        } else {
            if (!"".equals(authenticator.getAppOnlyOAuthTokenCache())) {
                accessToken = authenticator.getAppOnlyOAuthTokenCache();
            } else {
                /* If user is logged out and there's no app only OAuth token in the cache,
                 * we need to fetch one. To do so, we just send a garbage value which triggers
                 * a 401 error, onErrorResumeNext(), and subsequently,
                 * fetchAppOnlyOAuthTokenASync() in our Rx chain.
                 *
                 * A little hacky since it relies on a side effect, but works fine. */

                accessToken = "xyz";
            }
        }

        return BEARER + accessToken;
    }

    /* used only by fetchSubscribedPostsASync(). Less logic is required compared to
     * createAllPostsAndCommentsBearerAuth(), since this is only called when the user is
     * logged in. */
    private String createSubscribedPostsBearerAuth() {
        return BEARER + authenticator.getUserOAuthAccessTokenCache();
    }

    // endregion helper methods --------------------------------------------------------------------

    // region room methods and classes -------------------------------------------------------------

    /* we could use Rx instead of an ExecutorService to write to the Room database, but let's
     * use the ExecutorService just to demonstrate a different method of asynchronous DB access. */
    public void insertClickedPostId(ClickedPostId id) {
        Runnable runnable = () -> clickedPostIdDao.insertClickedPostId(id);

        executor.execute(runnable);
    }

    /* stream of clicked post IDs, this Observable is merged into PostsViewState objects */
    private Observable<String[]> fetchClickedPostIds() {
        return clickedPostIdDao
                .getAllClickedPostIds()
                .map(this::getArrayOfClickedPostIds);
    }

    /* works in conjunction with the mergeClickedPosts BiFunction. Sort of awkward. We first
     * transform a List of ClickedPostId objects into an array of Strings, and then the array back
     * into a List of Strings. Rough alternative would be to write a custom equals() method for the
     * ClickedPostId class so we can more easily check if a List of ClickedPostId objects contains
     * a particular post id (String). */
    private String[] getArrayOfClickedPostIds(List<ClickedPostId> input) {
        int size = input.size();
        String[] clickedPostIds = new String[size];

        for (int i = 0; i < size; i++) {
            clickedPostIds[i] = input.get(i).getClickedPostId();
        }

        return clickedPostIds;
    }

    /* see comments on getArrayOfClickedPostIds() */
    private final BiFunction<PostsViewState, String[], PostsViewState> mergeClickedPosts
            = (postsViewState, ids) -> {
        List list = Arrays.asList(ids);

        for (int i = 0; i < 25; i++) {
            if (list.contains(postsViewState.postData.get(i).id)) {
                postsViewState.hasBeenClicked[i] = true;
            }
        }

        return postsViewState;
    };

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

    private void setNetworkErrorsLiveData(Event<NetworkErrors> n) {
        networkErrorsLiveData.setValue(n);
    }

    public LiveData<Event<NetworkErrors>> getNetworkErrorsLiveData() {
        return networkErrorsLiveData;
    }

    //endregion event handling ---------------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return allPostsViewStateLiveData;
    }

    public LiveData<PostsViewState> getSubscribedPostsViewStateLiveData() {
        return subscribedPostsViewStateLiveData;
    }

    public LiveData<CommentsViewState> getCommentsViewStateLiveData() {
        return commentsViewStateLiveData;
    }

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return isUserLoggedInLiveData;
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
