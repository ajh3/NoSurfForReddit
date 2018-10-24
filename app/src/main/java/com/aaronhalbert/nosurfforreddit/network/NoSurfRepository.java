package com.aaronhalbert.nosurfforreddit.network;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.SingleLiveEvent;
import com.aaronhalbert.nosurfforreddit.room.ReadPostId;
import com.aaronhalbert.nosurfforreddit.room.ReadPostIdDao;
import com.aaronhalbert.nosurfforreddit.room.ReadPostIdRoomDatabase;
import com.aaronhalbert.nosurfforreddit.redditschema.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.redditschema.Listing;
import com.aaronhalbert.nosurfforreddit.redditschema.UserOAuthToken;

import java.util.List;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private static final String authHeader = okhttp3.Credentials.basic(CLIENT_ID, "");

    String previousCommentId;

    private ReadPostIdDao readPostIdDao;
    private LiveData<List<ReadPostId>> readPostIdLiveData;

    private MutableLiveData<String> userOAuthTokenLiveData = new MutableLiveData<>(); //TODO: convert to regular variable, I never observe this
    private MutableLiveData<String> userOAuthRefreshTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<String> appOnlyOAuthTokenLiveData = new MutableLiveData<>(); //TODO: convert to regular variable, I never observe this

    private MutableLiveData<Listing> allPostsLiveData = new MutableLiveData<>();
    private MutableLiveData<Listing> subscribedPostsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Listing>> commentsLiveData = new MutableLiveData<>();

    private SingleLiveEvent<Boolean> commentsFinishedLoadingLiveEvent = new SingleLiveEvent<>();

    private RetrofitInterface ri;

    private SharedPreferences preferences;

    public NoSurfRepository(Retrofit retrofit, SharedPreferences preferences, ReadPostIdRoomDatabase db) {

        this.preferences = preferences;

        ri = retrofit.create(RetrofitInterface.class);

        readPostIdDao = db.readPostIdDao();

        readPostIdLiveData = readPostIdDao.getAllReadPostIds(); //TODO: assigning this seems weird?
    }

    /* Called if the user has never logged in before, so user can browse /r/all */
    /* Also called to "refresh" the app-only token, there is no separate method */

    public void requestAppOnlyOAuthToken(final String callback, final String id) {
        ri.requestAppOnlyOAuthToken(OAUTH_BASE_URL, APP_ONLY_GRANT_TYPE, DEVICE_ID, authHeader)
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
                        refreshPostComments(id, false);
                        break;
                    case "":
                        break;
                }
            }

            @Override
            public void onFailure(Call<AppOnlyOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), "--> App-only auth call failed");
            }
        });
    }

    public void requestUserOAuthToken(String code) {
        ri.requestUserOAuthToken(OAUTH_BASE_URL, USER_GRANT_TYPE, code, REDIRECT_URI, authHeader)
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
                Log.e(getClass().toString(), "--> User auth call failed");
            }
        });
    }

    private void refreshExpiredUserOAuthToken(final String callback, final String id) {
        String userAccessRefreshToken = userOAuthRefreshTokenLiveData.getValue();

        ri.refreshExpiredUserOAuthToken(OAUTH_BASE_URL, USER_REFRESH_GRANT_TYPE, userAccessRefreshToken, authHeader)
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
                        refreshPostComments(id, true);
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), "--> Refresh auth call failed");
            }
        });
    }

    /* Can be called when user is logged in or out */

    public void refreshAllPosts(final boolean isUserLoggedIn) {
        final String accessToken;
        String bearerAuth;

        if (isUserLoggedIn) {
            Log.e(getClass().toString(), "--> user logged in, requesting all...");
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
                    Log.e(getClass().toString(), "--> response code 401 while requesting all, refreshing expired token...");
                    refreshExpiredUserOAuthToken("refreshAllPosts", null);
                } else if ((response.code() == 401) && (!isUserLoggedIn)) {
                    Log.e(getClass().toString(), "--> response code 401 while requesting app-only all, requesting new app-only token...");
                    requestAppOnlyOAuthToken("refreshAllPosts", null);
                } else {
                    allPostsLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.e(getClass().toString(), "--> refreshAllPosts call failed: " + t.toString());
            }
        });
    }

    /* Should only run when user is logged in */

    public void refreshSubscribedPosts(final boolean isUserLoggedIn) {
        String bearerAuth = "Bearer " + userOAuthTokenLiveData.getValue();

        if (isUserLoggedIn) {
            Log.e(getClass().toString(), "--> user logged in, requesting subscribed posts...");
            ri.refreshSubscribedPosts(bearerAuth).enqueue(new Callback<Listing>() {
                @Override
                public void onResponse(Call<Listing> call, Response<Listing> response) {
                    if (response.code() == 401) {
                        Log.e(getClass().toString(), "--> response code 401 while requesting subscribed posts, refreshing expired token...");
                        refreshExpiredUserOAuthToken("refreshSubscribedPosts", null);
                    } else {
                        subscribedPostsLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<Listing> call, Throwable t) {
                    Log.e(getClass().toString(), "--> refreshSubscribedPosts call failed: " + t.toString());
                }
            });
        } else {
            // do nothing
        }
    }

    /* Can be called when user is logged in or out */

    public void refreshPostComments(String id, final boolean isUserLoggedIn) {
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
            bearerAuth = "Bearer " + accessToken;
        } else {
            accessToken = appOnlyOAuthTokenLiveData.getValue();
            bearerAuth = "Bearer " + accessToken;
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
                    commentsFinishedLoadingLiveEvent.setValue(true);
                    commentsFinishedLoadingLiveEvent.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                Log.e(getClass().toString(), "--> refreshPostComments call failed: " + t.toString());
            }
        });
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

    public LiveData<List<ReadPostId>> getReadPostIdLiveData() {
        Log.e(getClass().toString(), "getReadPostIdLiveData called in repo");
        return readPostIdLiveData;
    }

    public void insertReadPostId(ReadPostId id) {
        new InsertAsyncTask(readPostIdDao).execute(id);
    }

    private static class InsertAsyncTask extends AsyncTask<ReadPostId, Void, Void> {
        private ReadPostIdDao asyncTaskDao;

        InsertAsyncTask(ReadPostIdDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ReadPostId... params) {
            asyncTaskDao.insertReadPostId(params[0]);
            return null;
        }
    }
}
