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
    private static final String KEY_USER_ACCESS_TOKEN = "userAccessToken";
    private static final String KEY_USER_ACCESS_REFRESH_TOKEN = "userAccessRefreshToken";
    private static final String AUTH_HEADER = okhttp3.Credentials.basic(CLIENT_ID, "");
    private static final String APP_ONLY_AUTH_CALL_FAILED = "App-only auth call failed";
    private static final String USER_AUTH_CALL_FAILED = "User auth call failed";
    private static final String REFRESH_AUTH_CALL_FAILED = "Refresh auth call failed";
    private static final String REFRESH_ALL_POSTS_CALL_FAILED = "refreshAllPosts call failed: ";
    private static final String REFRESH_SUBSCRIBED_POSTS_CALL_FAILED = "refreshSubscribedPosts call failed: ";
    private static final String REFRESH_POST_COMMENTS_CALL_FAILED = "refreshPostComments call failed: ";
    private static final String BEARER = "Bearer ";

    String previousCommentId;
    private LiveData<List<ClickedPostId>> clickedPostIdLiveData;

    private MutableLiveData<String> userOAuthTokenLiveData = new MutableLiveData<>(); //TODO: convert to regular variable, I never observe this
    private MutableLiveData<String> userOAuthRefreshTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<String> appOnlyOAuthTokenLiveData = new MutableLiveData<>(); //TODO: convert to regular variable, I never observe this

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

    /* Called if the user has never logged in before, so user can browse /r/all */
    /* Also called to "refresh" the app-only token, there is no separate method */

    public void requestAppOnlyOAuthToken(final String callback, final String id) {
        ri.requestAppOnlyOAuthToken(OAUTH_BASE_URL, APP_ONLY_GRANT_TYPE, DEVICE_ID, AUTH_HEADER)
                .enqueue(new Callback<AppOnlyOAuthToken>() {

            @Override
            public void onResponse(Call<AppOnlyOAuthToken> call, Response<AppOnlyOAuthToken> response) {
                String appOnlyAccessToken = response.body().getAccessToken();

                //"cache" token in a LiveData
                appOnlyOAuthTokenLiveData.setValue(appOnlyAccessToken);

                preferences
                        .edit()
                        .putString(KEY_APP_ONLY_TOKEN, appOnlyAccessToken)
                        .apply();

                switch (callback) {
                    case "refreshAllPosts":
                        refreshAllPosts(false);
                        break;
                    case "refreshPostComments":
                        fetchPostComments(id, false);
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

    public void requestUserOAuthToken(String code) {
        ri.requestUserOAuthToken(OAUTH_BASE_URL, USER_GRANT_TYPE, code, REDIRECT_URI, AUTH_HEADER)
                .enqueue(new Callback<UserOAuthToken>() {
            @Override
            public void onResponse(Call<UserOAuthToken> call, Response<UserOAuthToken> response) {
                String userAccessToken = response.body().getAccessToken();
                String userAccessRefreshToken = response.body().getRefreshToken();

                //"cache" tokens in a LiveData
                userOAuthTokenLiveData.setValue(userAccessToken);
                userOAuthRefreshTokenLiveData.setValue(userAccessRefreshToken);

                preferences
                        .edit()
                        .putString(KEY_USER_ACCESS_TOKEN, userAccessToken)
                        .putString(KEY_USER_ACCESS_REFRESH_TOKEN, userAccessRefreshToken)
                        .apply();

                refreshAllPosts(true);
                refreshSubscribedPosts(true);
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), USER_AUTH_CALL_FAILED);
            }
        });
    }

    private void refreshExpiredUserOAuthToken(final String callback, final String id) {
        String userAccessRefreshToken = userOAuthRefreshTokenLiveData.getValue();

        ri.refreshExpiredUserOAuthToken(OAUTH_BASE_URL, USER_REFRESH_GRANT_TYPE, userAccessRefreshToken, AUTH_HEADER)
                .enqueue(new Callback<UserOAuthToken>() {
            @Override
            public void onResponse(Call<UserOAuthToken> call, Response<UserOAuthToken> response) {
                String userAccessToken = response.body().getAccessToken();

                //"cache" token in a LiveData
                userOAuthTokenLiveData.setValue(userAccessToken);

                preferences
                        .edit()
                        .putString(KEY_USER_ACCESS_TOKEN, userAccessToken)
                        .apply();

                switch (callback) {
                    case "refreshAllPosts":
                        refreshAllPosts(true);
                        break;
                    case "refreshSubscribedPosts":
                        refreshSubscribedPosts(true);
                        break;
                    case "refreshPostComments":
                        fetchPostComments(id, true);
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), REFRESH_AUTH_CALL_FAILED);
            }
        });
    }

    /* Can be called when user is logged in or out */

    public void refreshAllPosts(final boolean isUserLoggedIn) {
        final String accessToken;
        String bearerAuth;

        if (isUserLoggedIn) {
            accessToken = userOAuthTokenLiveData.getValue();
            bearerAuth = "Bearer " + accessToken;
        } else {
            accessToken = appOnlyOAuthTokenLiveData.getValue();
            bearerAuth = "Bearer " + accessToken;
        }

        ri.refreshAllPosts(bearerAuth).enqueue(new Callback<Listing>() {
            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {
                if ((response.code() == 401) && (isUserLoggedIn)) {
                    refreshExpiredUserOAuthToken("refreshAllPosts", null);
                } else if ((response.code() == 401) && (!isUserLoggedIn)) {
                    requestAppOnlyOAuthToken("refreshAllPosts", null);
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

    /* Should only run when user is logged in */

    public void refreshSubscribedPosts(final boolean isUserLoggedIn) {
        String bearerAuth = "Bearer " + userOAuthTokenLiveData.getValue();

        if (isUserLoggedIn) {
            ri.refreshSubscribedPosts(bearerAuth).enqueue(new Callback<Listing>() {
                @Override
                public void onResponse(Call<Listing> call, Response<Listing> response) {
                    if (response.code() == 401) {
                        refreshExpiredUserOAuthToken("refreshSubscribedPosts", null);
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
            // do nothing
        }
    }

    /* Can be called when user is logged in or out */

    public void fetchPostComments(String id, final boolean isUserLoggedIn) {
        String accessToken;
        String bearerAuth;
        String idToPass;

        //to let refresh button refresh last comments
        if (id.equals("previous") && previousCommentId == null) {
            return;
        } else if (id.equals("previous")) {
            idToPass = previousCommentId;
        } else {
            previousCommentId = idToPass = id;
        }

        final String finalIdToPass = idToPass; // need a final String for the anonymous inner class

        if (isUserLoggedIn) {
            accessToken = userOAuthTokenLiveData.getValue();
            bearerAuth = BEARER + accessToken;
        } else {
            accessToken = appOnlyOAuthTokenLiveData.getValue();
            bearerAuth = BEARER + accessToken;
        }

        ri.refreshPostComments(bearerAuth, finalIdToPass).enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {

                if ((response.code() == 401) && (isUserLoggedIn)) {
                    refreshExpiredUserOAuthToken("refreshPostComments", finalIdToPass);
                } else if ((response.code() == 401) && (!isUserLoggedIn)) {
                    requestAppOnlyOAuthToken("refreshPostComments", finalIdToPass);
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

    //TODO: this doesn't really belong in repository (?)
    public void dispatchCommentsLiveDataChangedEvent() {
        commentsFinishedLoadingLiveEvent.setValue(true);
    }

    //TODO: this doesn't really belong in repository (?)
    public void consumeCommentsLiveDataChangedEvent() {
        commentsFinishedLoadingLiveEvent.setValue(false);
    }

    public void logout() {

        userOAuthTokenLiveData.setValue("");
        userOAuthRefreshTokenLiveData.setValue("");

        preferences
                .edit()
                .putString(KEY_USER_ACCESS_TOKEN, "")
                .putString(KEY_USER_ACCESS_REFRESH_TOKEN, "")
                .apply();
    }

    public void initializeTokensFromSharedPrefs() {

        String userOAuthToken = preferences.getString(KEY_USER_ACCESS_TOKEN, null);
        String userOAuthRefreshToken = preferences.getString(KEY_USER_ACCESS_REFRESH_TOKEN, null);

        userOAuthTokenLiveData.setValue(userOAuthToken);
        userOAuthRefreshTokenLiveData.setValue(userOAuthRefreshToken);
    }

    public LiveData<Listing> getAllPostsLiveData() {
        return allPostsLiveData;
    }

    public LiveData<Listing> getSubscribedPostsLiveData() {
        return subscribedPostsLiveData;
    }

    public LiveData<List<Listing>> getCommentsLiveData() {
        return commentsLiveData;
    }

    public SingleLiveEvent<Boolean> getCommentsFinishedLoadingLiveEvent() {
        return commentsFinishedLoadingLiveEvent;
    }

    public LiveData<String> getUserOAuthRefreshTokenLiveData() {
        return userOAuthRefreshTokenLiveData;
    }

    public LiveData<List<ClickedPostId>> getClickedPostIdLiveData() {
        return clickedPostIdLiveData;
    }

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
}
