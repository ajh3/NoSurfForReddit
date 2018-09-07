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

    private static NoSurfRepository repositoryInstance = null;

    private static Context context;

    private MutableLiveData<Listing> allPostsLiveData = new MutableLiveData<Listing>();
    private MutableLiveData<Listing> homePostsLiveData = new MutableLiveData<Listing>();
    private MutableLiveData<List<Listing>> commentsLiveData = new MutableLiveData<List<Listing>>();

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

    public void requestAppOnlyOAuthToken() {
        ri.requestAppOnlyOAuthToken(OAUTH_BASE_URL, APP_ONLY_GRANT_TYPE, DEVICE_ID, authHeader).enqueue(new Callback<AppOnlyOAuthToken>() {
            @Override
            public void onResponse(Call<AppOnlyOAuthToken> call, Response<AppOnlyOAuthToken> response) {
                String appOnlyAccessToken = response.body().getAccessToken();
                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);

                preferences
                        .edit()
                        .putString(KEY_APP_ONLY_TOKEN, appOnlyAccessToken)
                        .apply();
            }

            @Override
            public void onFailure(Call<AppOnlyOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), "App-only auth call failed");
            }
        });
    }

    public void requestUserOAuthToken(String code) {
        ri.requestUserOAuthToken(OAUTH_BASE_URL, USER_GRANT_TYPE, code, REDIRECT_URI, authHeader).enqueue(new Callback<UserOAuthToken>() {
            @Override
            public void onResponse(Call<UserOAuthToken> call, Response<UserOAuthToken> response) {
                String userAccessToken = response.body().getAccessToken();
                String userAccessRefreshToken = response.body().getRefreshToken();
                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);

                preferences
                        .edit()
                        .putString(KEY_USER_ACCESS_TOKEN, userAccessToken)
                        .putString(KEY_USER_ACCESS_REFRESH_TOKEN, userAccessRefreshToken)
                        .apply();
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), "User auth call failed");
            }
        });
    }

    public void refreshExpiredUserOAuthToken() {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);
        String userAccessRefreshToken = preferences.getString(KEY_USER_ACCESS_REFRESH_TOKEN, null);

        ri.refreshExpiredUserOAuthToken(OAUTH_BASE_URL, USER_REFRESH_GRANT_TYPE, userAccessRefreshToken, authHeader).enqueue(new Callback<UserOAuthToken>() {
            @Override
            public void onResponse(Call<UserOAuthToken> call, Response<UserOAuthToken> response) {
                String userAccessToken = response.body().getAccessToken();

                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);

                preferences
                        .edit()
                        .putString(KEY_USER_ACCESS_TOKEN, userAccessToken)
                        .apply();
            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.e(getClass().toString(), "Refresh auth call failed");
            }
        });
    }

    public void requestAllSubredditsListing(boolean isUserLoggedIn) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);
        String accessToken;
        String bearerAuth;

        if (isUserLoggedIn) {
            accessToken = preferences.getString(KEY_USER_ACCESS_TOKEN, null);
            bearerAuth = "Bearer " + accessToken;
        } else {
            accessToken = preferences.getString(KEY_APP_ONLY_TOKEN, null);
            bearerAuth = "Bearer " + accessToken;
        }

        ri.requestAllSubredditsListing(bearerAuth).enqueue(new Callback<Listing>() {
            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {
                allPostsLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.e(getClass().toString(), "requestAllSubredditsListing call failed: " + t.toString());
            }
        });
    }

    /* Should only be called when user is logged in */

    public void requestHomeSubredditsListing(boolean isUserLoggedIn) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);
        String userAccessToken = preferences.getString(KEY_USER_ACCESS_TOKEN, null);
        String bearerAuth = "Bearer " + userAccessToken;

        if (isUserLoggedIn) {
            ri.requestHomeSubredditsListing(bearerAuth).enqueue(new Callback<Listing>() {
                @Override
                public void onResponse(Call<Listing> call, Response<Listing> response) {
                    homePostsLiveData.setValue(response.body());
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

    public void requestPostCommentsListing(String id, boolean isUserLoggedIn) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "oauth", context.MODE_PRIVATE);
        String accessToken;
        String bearerAuth;

        if (isUserLoggedIn) {
            accessToken = preferences.getString(KEY_USER_ACCESS_TOKEN, null);
            bearerAuth = "Bearer " + accessToken;
        } else {
            accessToken = preferences.getString(KEY_APP_ONLY_TOKEN, null);
            bearerAuth = "Bearer " + accessToken;
        }

        ri.requestPostCommentsListing(bearerAuth, id).enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                commentsLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                Log.e(getClass().toString(), "requestPostCommentsListing call failed: " + t.toString());
            }
        });
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
}
