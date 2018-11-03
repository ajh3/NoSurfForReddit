package com.aaronhalbert.nosurfforreddit.network;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.SingleLiveEvent;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostIdDao;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostIdRoomDatabase;
import com.aaronhalbert.nosurfforreddit.network.redditschema.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.network.redditschema.Listing;
import com.aaronhalbert.nosurfforreddit.network.redditschema.UserOAuthToken;

import java.util.List;

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
    private static final int RESPONSE_CODE_401 = 401;

    // caches let us keep working during asynchronous writes to SharedPrefs
    private String userOAuthAccessTokenCache = "";
    private String userOAuthRefreshTokenCache = "";
    private String appOnlyOAuthToken = "";
    private boolean isUserLoggedInCache;

    private final LiveData<List<ClickedPostId>> clickedPostIdsLiveData;
    private final MutableLiveData<Listing> allPostsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Listing> subscribedPostsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> commentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserLoggedInLiveData = new MutableLiveData<>();

    private final SingleLiveEvent<Boolean> commentsFinishedLoadingLiveEvents = new SingleLiveEvent<>();

    private final RetrofitInterface ri;
    private final ClickedPostIdDao clickedPostIdDao;
    private final SharedPreferences preferences;

    public NoSurfRepository(Retrofit retrofit,
                            SharedPreferences preferences,
                            ClickedPostIdRoomDatabase db) {
        this.preferences = preferences;
        ri = retrofit.create(RetrofitInterface.class);
        clickedPostIdDao = db.clickedPostIdDao();
        clickedPostIdsLiveData = clickedPostIdDao.getAllClickedPostIds();
    }

    // region network auth calls -------------------------------------------------------------------

    /* Logged-out (aka anonymous, aka app-only users require an anonymous token to interact with
     * the Reddit API and view posts and comments from r/all. This token is provided by
     * fetchAppOnlyOAuthTokenASync and requires no user credentials.
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
                appOnlyOAuthToken = response.body().getAccessToken();
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
      * but this use case requires a user OAuth token from fetchUserOAuthTokenASync.
      *
      * Note that after the user is logged in, their user token is additionally used for viewing
      * r/all, instead of the previously-fetched anonymous token */
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
            if (!"".equals(appOnlyOAuthToken)) {
                accessToken = appOnlyOAuthToken;
            } else {
                /* garbage value ensures the below call gets a 401 error and thus executes
                 * fetchAppOnlyOAuthTokenASync and its callback in the right order, instead
                 * of the call failing */
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
             * background "timer" task that refreshes them every X minutes */
            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {
                if ((response.code() == RESPONSE_CODE_401) && (isUserLoggedInCache)) {
                    refreshExpiredUserOAuthTokenASync(NetworkCallbacks.FETCH_ALL_POSTS_ASYNC, "");
                } else if (response.code() == RESPONSE_CODE_401) {
                    fetchAppOnlyOAuthTokenASync(NetworkCallbacks.FETCH_ALL_POSTS_ASYNC, "");
                } else {
                    allPostsLiveData.setValue(response.body());
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
                        subscribedPostsLiveData.setValue(response.body());
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

    /* get a post's comments; works for either logged-in or logged-out users */
    public void fetchPostCommentsASync(final String id) {

        //noinspection StatementWithEmptyBody
        if (!"".equals(id)) {
            String accessToken;
            String bearerAuth;

            if (isUserLoggedInCache) {
                accessToken = userOAuthAccessTokenCache;
            } else {
                accessToken = appOnlyOAuthToken;
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
                        commentsLiveData.setValue(response.body());
                        dispatchCommentsLiveDataChangedEvent();
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
            // need to explicitly call this so ContainerFragment knows how to set itself up
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

    // region event handling -----------------------------------------------------------------------

    public SingleLiveEvent<Boolean> getCommentsFinishedLoadingLiveEvents() {
        return commentsFinishedLoadingLiveEvents;
    }

    private void dispatchCommentsLiveDataChangedEvent() {
        commentsFinishedLoadingLiveEvents.setValue(true);
    }

    public void consumeCommentsLiveDataChangedEvent() {
        commentsFinishedLoadingLiveEvents.setValue(false);
    }

    //endregion event handling ---------------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    public LiveData<Listing> getAllPostsLiveData() {
        return allPostsLiveData;
    }

    public LiveData<Listing> getSubscribedPostsLiveData() {
        return subscribedPostsLiveData;
    }

    public LiveData<List<Listing>> getCommentsLiveData() {
        return commentsLiveData;
    }

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return isUserLoggedInLiveData;
    }

    public LiveData<List<ClickedPostId>> getClickedPostIdsLiveData() {
        return clickedPostIdsLiveData;
    }

    // endregion getter methods --------------------------------------------------------------------

    // region room methods and classes -------------------------------------------------------------

    public void insertClickedPostId(ClickedPostId id) {
        new InsertAsyncTask(clickedPostIdDao).execute(id);
    }

    private static class InsertAsyncTask extends AsyncTask<ClickedPostId, Void, Void> {
        private final ClickedPostIdDao asyncTaskDao;

        InsertAsyncTask(ClickedPostIdDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ClickedPostId... params) {
            asyncTaskDao.insertClickedPostId(params[0]);
            return null;
        }
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
