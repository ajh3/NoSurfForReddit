package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.reddit.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;
import com.aaronhalbert.nosurfforreddit.reddit.UserOAuthToken;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NoSurfRepository {
    private static NoSurfRepository repositoryInstance = null;

    private static final String APP_ONLY_GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private static final String USER_GRANT_TYPE = "authorization_code";
    private static final String DEVICE_ID = "DO_NOT_TRACK_THIS_DEVICE";
    private static final String OAUTH_BASE_URL = "https://www.reddit.com/api/v1/access_token";
    private static final String API_BASE_URL = "https://oauth.reddit.com/";
    private static final String REDIRECT_URI = "nosurfforreddit://oauth";
    private static final String CLIENT_ID = "jPF59UF5MbMkWg";

    private static final String authHeader = okhttp3.Credentials.basic(CLIENT_ID, "");


    private String appOnlyAccessToken;
    private String userAccessToken;

    private MutableLiveData<Listing> allPostsLiveData = new MutableLiveData<Listing>();
    private MutableLiveData<Listing> homePostsLiveData = new MutableLiveData<Listing>();

    private HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(logging);

    private NoSurfRepository() { }

    public static NoSurfRepository getInstance() {
        if (repositoryInstance == null) {
            repositoryInstance = new NoSurfRepository();
        }

        return repositoryInstance;
    }

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build();

    private RetrofitInterface ri = retrofit.create(RetrofitInterface.class);

    public void requestAppOnlyOAuthToken() {
        ri.requestAppOnlyOAuthToken(OAUTH_BASE_URL, APP_ONLY_GRANT_TYPE, DEVICE_ID, authHeader).enqueue(new Callback<AppOnlyOAuthToken>() {
            @Override
            public void onResponse(Call<AppOnlyOAuthToken> call, Response<AppOnlyOAuthToken> response) {
                appOnlyAccessToken = response.body().getAccessToken();
                requestAllSubredditsListing();
            }

            @Override
            public void onFailure(Call<AppOnlyOAuthToken> call, Throwable t) {
                Log.d(getClass().toString(), "Auth call failed");
            }
        });
    }


    public void requestUserOAuthToken(String code) {



        Log.e(getClass().toString(), authHeader);

        ri.requestUserOAuthToken(OAUTH_BASE_URL, USER_GRANT_TYPE, code, REDIRECT_URI, authHeader).enqueue(new Callback<UserOAuthToken>() {
            @Override
            public void onResponse(Call<UserOAuthToken> call, Response<UserOAuthToken> response) {
                userAccessToken = response.body().getAccessToken();

                Log.e(getClass().toString(), "ZZZZ: " + userAccessToken);

                requestHomeSubredditsListing();


            }

            @Override
            public void onFailure(Call<UserOAuthToken> call, Throwable t) {
                Log.d(getClass().toString(), "User auth call failed");
            }
        });
    }




    public void requestAllSubredditsListing() {

        String bearerAuth = "Bearer " + appOnlyAccessToken;

        ri.requestAllSubredditsListing(bearerAuth).enqueue(new Callback<Listing>() {

            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {

                allPostsLiveData.setValue(response.body());

            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.d(getClass().toString(), "requestAllSubredditsListing call failed: " + t.toString());
            }
        });


    }




    public void requestHomeSubredditsListing() {

        String bearerAuth = "Bearer " + userAccessToken;

        ri.requestHomeSubredditsListing(bearerAuth).enqueue(new Callback<Listing>() {

            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {

                homePostsLiveData.setValue(response.body());

            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.d(getClass().toString(), "requestHomeSubredditsListing call failed: " + t.toString());
            }
        });


    }



    public LiveData<Listing> getAllPostsLiveData() {
        return allPostsLiveData;
    }

    public LiveData<Listing> getHomePostsLiveData() {
        return homePostsLiveData;
    }

}
