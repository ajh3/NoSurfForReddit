package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.reddit.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;
import com.aaronhalbert.nosurfforreddit.reddit.UserOAuthToken;

import java.util.List;

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
    private static final String API_BASE_URL = "https://oauth.reddit.com/";
    private static final String REDIRECT_URI = "nosurfforreddit://oauth";
    private static final String CLIENT_ID = "jPF59UF5MbMkWg";
    private static final String KEY_APP_ONLY_TOKEN = "appOnlyAccessToken";
    private static final String KEY_USER_ACCESS_TOKEN = "userAccessToken";
    private static final String KEY_USER_ACCESS_REFRESH_TOKEN = "userAccessRefreshToken";
    private static final String authHeader = okhttp3.Credentials.basic(CLIENT_ID, "");

    String previousCommentId;

    private static NoSurfRepository repositoryInstance = null;

    private static Context context;

    private MutableLiveData<String> userOAuthTokenLiveData = new MutableLiveData<>(); //TODO: convert to regular variable, I never observe this
    private MutableLiveData<String> userOAuthRefreshTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<String> appOnlyOAuthTokenLiveData = new MutableLiveData<>(); //TODO: convert to regular variable, I never observe this

    private MutableLiveData<Listing> allPostsLiveData = new MutableLiveData<>();
    private MutableLiveData<Listing> homePostsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Listing>> commentsLiveData = new MutableLiveData<>();

    private HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(logging);

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build();

    private RetrofitInterface ri = retrofit.create(RetrofitInterface.class);

    private NoSurfRepository() { }

    public static NoSurfRepository getInstance(Context context) {
        if (repositoryInstance == null) {
            repositoryInstance = new NoSurfRepository();

            NoSurfRepository.context = context;
        }
        return repositoryInstance;
    }

    /* Called if the user has never logged in before, so user can browse /r/all */
    /* Also called to "refresh" the app-only token, there is no separate method */

    public void requestAppOnlyOAuthToken(final String callback, final String id) {
        ri.requestAppOnlyOAuthToken(OAUTH_BASE_URL, APP_ONLY_GRANT_TYPE, DEVICE_ID, authHeader)
                .enqueue(new Callback<AppOnlyOAuthToken>() {

            @Override
            public void onResponse(Call<AppOnlyOAuthToken> call, Response<AppOnlyOAuthToken> response) {
                String appOnlyAccessToken = response.body().getAccessToken();
                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);

                //"cache" token in a LiveData
                appOnlyOAuthTokenLiveData.setValue(appOnlyAccessToken);

                preferences
                        .edit()
                        .putString(KEY_APP_ONLY_TOKEN, appOnlyAccessToken)
                        .apply();

                switch (callback) {
                    case "requestAllSubredditsListing":
                        requestAllSubredditsListing(false);
                        break;
                    case "requestPostCommentsListing":
                        requestPostCommentsListing(id, false);
                        break;
                    case "":
                        break;
                }
            }

            @Override
            public void onFailure(Call<AppOnlyOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), "App-only auth call failed");
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
                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);

                //"cache" tokens in a LiveData
                userOAuthTokenLiveData.setValue(userAccessToken);
                userOAuthRefreshTokenLiveData.setValue(userAccessRefreshToken);

                preferences
                        .edit()
                        .putString(KEY_USER_ACCESS_TOKEN, userAccessToken)
                        .putString(KEY_USER_ACCESS_REFRESH_TOKEN, userAccessRefreshToken)
                        .apply();

                requestAllSubredditsListing(true);
                requestHomeSubredditsListing(true);
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), "User auth call failed");
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

                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);

                //"cache" token in a LiveData
                userOAuthTokenLiveData.setValue(userAccessToken);

                preferences
                        .edit()
                        .putString(KEY_USER_ACCESS_TOKEN, userAccessToken)
                        .apply();

                switch (callback) {
                    case "requestAllSubredditsListing":
                        requestAllSubredditsListing(true);
                        break;
                    case "requestHomeSubredditsListing":
                        requestHomeSubredditsListing(true);
                        break;
                    case "requestPostCommentsListing":
                        requestPostCommentsListing(id, true);
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), "Refresh auth call failed");
            }
        });
    }

    /* Can be called when user is logged in or out */

    public void requestAllSubredditsListing(final boolean isUserLoggedIn) {
        final String accessToken;
        String bearerAuth;

        if (isUserLoggedIn) {
            accessToken = userOAuthTokenLiveData.getValue();
            bearerAuth = "Bearer " + accessToken;
        } else {
            accessToken = appOnlyOAuthTokenLiveData.getValue();
            bearerAuth = "Bearer " + accessToken;
        }

        ri.requestAllSubredditsListing(bearerAuth).enqueue(new Callback<Listing>() {
            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {
                if ((response.code() == 401) && (isUserLoggedIn)) {
                    refreshExpiredUserOAuthToken("requestAllSubredditsListing", null);
                } else if ((response.code() == 401) && (!isUserLoggedIn)) {
                    requestAppOnlyOAuthToken("requestAllSubredditsListing", null);
                } else {
                    allPostsLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.e(getClass().toString(), "requestAllSubredditsListing call failed: " + t.toString());
            }
        });
    }

    /* Should only run when user is logged in */

    public void requestHomeSubredditsListing(final boolean isUserLoggedIn) {
        String bearerAuth = "Bearer " + userOAuthTokenLiveData.getValue();

        if (isUserLoggedIn) {
            ri.requestHomeSubredditsListing(bearerAuth).enqueue(new Callback<Listing>() {
                @Override
                public void onResponse(Call<Listing> call, Response<Listing> response) {
                    if (response.code() == 401) {
                        refreshExpiredUserOAuthToken("requestHomeSubredditsListing", null);
                    } else {
                        homePostsLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(Call<Listing> call, Throwable t) {
                    Log.e(getClass().toString(), "requestHomeSubredditsListing call failed: " + t.toString());
                }
            });
        } else {
            // do nothing
        }
    }

    /* Can be called when user is logged in or out */

    public void requestPostCommentsListing(String id, final boolean isUserLoggedIn) {
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

        Log.e(getClass().toString(), finalIdToPass);

        if (isUserLoggedIn) {
            accessToken = userOAuthTokenLiveData.getValue();
            bearerAuth = "Bearer " + accessToken;
        } else {
            accessToken = appOnlyOAuthTokenLiveData.getValue();
            bearerAuth = "Bearer " + accessToken;
        }

        ri.requestPostCommentsListing(bearerAuth, finalIdToPass).enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                if ((response.code() == 401) && (isUserLoggedIn)) {
                    refreshExpiredUserOAuthToken("requestPostCommentsListing", finalIdToPass);
                } else if ((response.code() == 401) && (!isUserLoggedIn)) {
                    requestAppOnlyOAuthToken("requestPostCommentsListing", finalIdToPass);
                } else {
                    commentsLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                Log.e(getClass().toString(), "requestPostCommentsListing call failed: " + t.toString());
            }
        });
    }

    public void logout() {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);

        userOAuthTokenLiveData.setValue("");
        userOAuthRefreshTokenLiveData.setValue("");

        preferences
                .edit()
                .putString(KEY_USER_ACCESS_TOKEN, "")
                .putString(KEY_USER_ACCESS_REFRESH_TOKEN, "")
                .apply();
    }

    public void initializeTokensFromSharedPrefs() {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);

        String userOAuthToken = preferences.getString(KEY_USER_ACCESS_TOKEN, null);
        String userOAuthRefreshToken = preferences.getString(KEY_USER_ACCESS_REFRESH_TOKEN, null);

        userOAuthTokenLiveData.setValue(userOAuthToken);
        userOAuthRefreshTokenLiveData.setValue(userOAuthRefreshToken);
    }




    public LiveData<Listing> getAllPostsLiveData() {
        return allPostsLiveData;
    }

    public LiveData<Listing> getHomePostsLiveData() {
        return homePostsLiveData;
    }

    public LiveData<List<Listing>> getCommentsLiveData() {
        return commentsLiveData;
    }

    public LiveData<String> getUserOAuthRefreshTokenLiveData() {
        return userOAuthRefreshTokenLiveData;
    }
}
