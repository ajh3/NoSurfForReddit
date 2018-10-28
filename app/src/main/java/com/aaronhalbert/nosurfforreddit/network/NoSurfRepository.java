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
import com.aaronhalbert.nosurfforreddit.redditschema.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.redditschema.Listing;
import com.aaronhalbert.nosurfforreddit.redditschema.UserOAuthToken;

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
    private static final String KEY_APP_ONLY_TOKEN = "appOnlyAccessToken";
    private static final String KEY_USER_OAUTH_ACCESS_TOKEN = "userAccessToken";
    private static final String KEY_USER_OAUTH_REFRESH_TOKEN = "userAccessRefreshToken";
    private static final String AUTH_HEADER = okhttp3.Credentials.basic(CLIENT_ID, "");
    private static final String APP_ONLY_AUTH_CALL_FAILED = "App-only auth call failed";
    private static final String USER_AUTH_CALL_FAILED = "User auth call failed";
    private static final String REFRESH_AUTH_CALL_FAILED = "Refresh auth call failed";
    private static final String REFRESH_ALL_POSTS_CALL_FAILED = "fetchAllPostsSync call failed: ";
    private static final String REFRESH_SUBSCRIBED_POSTS_CALL_FAILED = "fetchSubscribedPostsSync call failed: ";
    private static final String REFRESH_POST_COMMENTS_CALL_FAILED = "fetchPostCommentsSync call failed: ";
    private static final String BEARER = "Bearer ";
    private static final int RESPONSE_CODE_401 = 401;

    private String userOAuthAccessToken;
    private String appOnlyOAuthToken;

    private LiveData<List<ClickedPostId>> clickedPostIdLiveData;
    private MutableLiveData<String> userOAuthRefreshTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<Listing> allPostsLiveData = new MutableLiveData<>();
    private MutableLiveData<Listing> subscribedPostsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Listing>> commentsLiveData = new MutableLiveData<>();

    private SingleLiveEvent<Boolean> commentsFinishedLoadingLiveEvent = new SingleLiveEvent<>();

    private RetrofitInterface ri;
    private ClickedPostIdDao clickedPostIdDao;
    private SharedPreferences preferences;

    public NoSurfRepository(Retrofit retrofit, SharedPreferences preferences, ClickedPostIdRoomDatabase db) {
        this.preferences = preferences;
        ri = retrofit.create(RetrofitInterface.class);
        clickedPostIdDao = db.clickedPostIdDao();
        clickedPostIdLiveData = clickedPostIdDao.getAllClickedPostIds(); //TODO: assigning this seems weird?
    }

    // region network auth calls -------------------------------------------------------------------

    /* Called if the user has never logged in before, so user can browse /r/all anonymously */
    /* Also called to refresh the anonymous app-only token when it expires */
    //TODO: eliminate need to pass ID by stashing it in vm?
    public void fetchAppOnlyOAuthTokenSync(final String callback, final String id) {
        ri.fetchAppOnlyOAuthTokenSync(OAUTH_BASE_URL, APP_ONLY_GRANT_TYPE, DEVICE_ID, AUTH_HEADER)
                .enqueue(new Callback<AppOnlyOAuthToken>() {

            @Override
            public void onResponse(Call<AppOnlyOAuthToken> call, Response<AppOnlyOAuthToken> response) {
                appOnlyOAuthToken = response.body().getAccessToken();

                preferences
                        .edit()
                        .putString(KEY_APP_ONLY_TOKEN, appOnlyOAuthToken)
                        .apply();

                //TODO: convert to enum
                switch (callback) {
                    case "fetchAllPostsSync":
                        fetchAllPostsSync(false);
                        break;
                    case "fetchPostCommentsSync":
                        fetchPostCommentsSync(id, false);
                        break;
                    case "":
                        break;
                }
            }

            @Override
            public void onFailure(Call<AppOnlyOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), APP_ONLY_AUTH_CALL_FAILED);
            }
        });
    }

    public void fetchUserOAuthTokenSync(String code) {
        ri.fetchUserOAuthTokenSync(OAUTH_BASE_URL, USER_GRANT_TYPE, code, REDIRECT_URI, AUTH_HEADER)
                .enqueue(new Callback<UserOAuthToken>() {

            @Override
            public void onResponse(Call<UserOAuthToken> call, Response<UserOAuthToken> response) {
                userOAuthAccessToken = response.body().getAccessToken();
                String userOAuthRefreshToken = response.body().getRefreshToken();

                //TODO: handle login status such that this LiveData is no longer needed
                userOAuthRefreshTokenLiveData.setValue(userOAuthRefreshToken);

                preferences
                        .edit()
                        .putString(KEY_USER_OAUTH_ACCESS_TOKEN, userOAuthAccessToken)
                        .putString(KEY_USER_OAUTH_REFRESH_TOKEN, userOAuthRefreshToken)
                        .apply();

                fetchAllPostsSync(true);
                fetchSubscribedPostsSync(true);
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), USER_AUTH_CALL_FAILED);
            }
        });
    }

    //TODO: eliminate need to pass ID by stashing it in vm?
    private void refreshExpiredUserOAuthTokenSync(final String callback, final String id) {
        String userOAuthRefreshToken = userOAuthRefreshTokenLiveData.getValue();

        ri.refreshExpiredUserOAuthTokenSync(OAUTH_BASE_URL, USER_REFRESH_GRANT_TYPE, userOAuthRefreshToken, AUTH_HEADER)
                .enqueue(new Callback<UserOAuthToken>() {

            @Override
            public void onResponse(Call<UserOAuthToken> call, Response<UserOAuthToken> response) {
                userOAuthAccessToken = response.body().getAccessToken();

                preferences
                        .edit()
                        .putString(KEY_USER_OAUTH_ACCESS_TOKEN, userOAuthAccessToken)
                        .apply();

                //TODO: convert to enum
                switch (callback) {
                    case "fetchAllPostsSync":
                        fetchAllPostsSync(true);
                        break;
                    case "fetchSubscribedPostsSync":
                        fetchSubscribedPostsSync(true);
                        break;
                    case "fetchPostCommentsSync":
                        fetchPostCommentsSync(id, true);
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

    public void fetchAllPostsSync(final boolean isUserLoggedIn) {
        String accessToken;
        String bearerAuth;

        if (isUserLoggedIn) {
            accessToken = userOAuthAccessToken;
        } else {
            accessToken = appOnlyOAuthToken;
        }

        bearerAuth = BEARER + accessToken;

        ri.fetchAllPostsSync(bearerAuth).enqueue(new Callback<Listing>() {

            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {
                if ((response.code() == RESPONSE_CODE_401) && (isUserLoggedIn)) {
                    refreshExpiredUserOAuthTokenSync("fetchAllPostsSync", null);
                } else if ((response.code() == RESPONSE_CODE_401) && (!isUserLoggedIn)) {
                    fetchAppOnlyOAuthTokenSync("fetchAllPostsSync", null);
                } else {
                    allPostsLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.e(getClass().toString(), REFRESH_ALL_POSTS_CALL_FAILED + t.toString());
            }
        });
    }

    public void fetchSubscribedPostsSync(final boolean isUserLoggedIn) {
        String bearerAuth = BEARER + userOAuthAccessToken;

        if (isUserLoggedIn) {
            ri.fetchSubscribedPostsSync(bearerAuth).enqueue(new Callback<Listing>() {

                @Override
                public void onResponse(Call<Listing> call, Response<Listing> response) {
                    if (response.code() == RESPONSE_CODE_401) {
                        refreshExpiredUserOAuthTokenSync("fetchSubscribedPostsSync", null);
                    } else {
                        subscribedPostsLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<Listing> call, Throwable t) {
                    Log.e(getClass().toString(), REFRESH_SUBSCRIBED_POSTS_CALL_FAILED + t.toString());
                }
            });
        } else {
            // do nothing if user is logged out
        }
    }

    public void fetchPostCommentsSync(final String id, final boolean isUserLoggedIn) {
        String accessToken;
        String bearerAuth;

        if (isUserLoggedIn) {
            accessToken = userOAuthAccessToken;
        } else {
            accessToken = appOnlyOAuthToken;
        }

        bearerAuth = BEARER + accessToken;

        ri.fetchPostCommentsSync(bearerAuth, id).enqueue(new Callback<List<Listing>>() {

            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                if ((response.code() == RESPONSE_CODE_401) && (isUserLoggedIn)) {
                    refreshExpiredUserOAuthTokenSync("fetchPostCommentsSync", id);
                } else if ((response.code() == RESPONSE_CODE_401) && (!isUserLoggedIn)) {
                    fetchAppOnlyOAuthTokenSync("fetchPostCommentsSync", id);
                } else {
                    commentsLiveData.setValue(response.body());
                    dispatchCommentsLiveDataChangedEvent();
                }

            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                Log.e(getClass().toString(), REFRESH_POST_COMMENTS_CALL_FAILED + t.toString());
            }
        });
    }

    // endregion network data calls ----------------------------------------------------------------

    // region init/de-init methods -----------------------------------------------------------------

    //TODO: this doesn't really belong in repository (?)
    public void initializeTokensFromSharedPrefs() {
        userOAuthAccessToken = preferences
                .getString(KEY_USER_OAUTH_ACCESS_TOKEN, null);
        String userOAuthRefreshToken = preferences
                .getString(KEY_USER_OAUTH_REFRESH_TOKEN, null);

        userOAuthRefreshTokenLiveData.setValue(userOAuthRefreshToken);
    }

    //TODO: this doesn't really belong in repository (?)
    public void logout() {
        userOAuthAccessToken = "";
        userOAuthRefreshTokenLiveData.setValue("");

        preferences
                .edit()
                .putString(KEY_USER_OAUTH_ACCESS_TOKEN, "")
                .putString(KEY_USER_OAUTH_REFRESH_TOKEN, "")
                .apply();
    }

    // endregion init/de-init methods --------------------------------------------------------------

    // region event handling -----------------------------------------------------------------------

    public SingleLiveEvent<Boolean> getCommentsFinishedLoadingLiveEvent() {
        return commentsFinishedLoadingLiveEvent;
    }

    //TODO: this doesn't really belong in repository (?)
    public void dispatchCommentsLiveDataChangedEvent() {
        commentsFinishedLoadingLiveEvent.setValue(true);
    }

    //TODO: this doesn't really belong in repository (?)
    public void consumeCommentsLiveDataChangedEvent() {
        commentsFinishedLoadingLiveEvent.setValue(false);
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

    public LiveData<String> getUserOAuthRefreshTokenLiveData() {
        return userOAuthRefreshTokenLiveData;
    }

    public LiveData<List<ClickedPostId>> getClickedPostIdLiveData() {
        return clickedPostIdLiveData;
    }

    // endregion getter methods --------------------------------------------------------------------

    // region room methods and classes -------------------------------------------------------------

    public void insertClickedPostId(ClickedPostId id) {
        new InsertAsyncTask(clickedPostIdDao).execute(id);
    }

    private static class InsertAsyncTask extends AsyncTask<ClickedPostId, Void, Void> {
        private ClickedPostIdDao asyncTaskDao;

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
}
