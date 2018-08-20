package com.aaronhalbert.meteorforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repository {
    private static Repository repositoryInstance = null;

    private static final String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private static final String DEVICE_ID = "DO_NOT_TRACK_THIS_DEVICE";
    private static final String OAUTH_BASE_URL = "https://www.reddit.com/api/v1/access_token";
    private static final String API_BASE_URL = "https://oauth.reddit.com/";
    //private static final String CLIENT_ID = "jPF59UF5MbMkWg";

    private String accessToken;

    private MutableLiveData<String[]> titleLiveData = new MutableLiveData<String[]>();

    private HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(logging);

    private Repository() { }

    public static Repository getInstance() {
        if (repositoryInstance == null) repositoryInstance = new Repository();

        return repositoryInstance;
    }

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build();

    private RetrofitInterface ri = retrofit.create(RetrofitInterface.class);

    public void requestAppOnlyOAuthToken() {
        ri.requestAppOnlyOAuthToken(OAUTH_BASE_URL, GRANT_TYPE, DEVICE_ID).enqueue(new Callback<AppOnlyOAuthToken>() {
            @Override
            public void onResponse(Call<AppOnlyOAuthToken> call, Response<AppOnlyOAuthToken> response) {
                accessToken = response.body().getAccess_token();
                requestSubRedditListing();
            }

            @Override
            public void onFailure(Call<AppOnlyOAuthToken> call, Throwable t) {
                Log.d(getClass().toString(), "Auth call failed");
            }
        });
    }

    public void requestSubRedditListing() {
        ri.requestSubRedditListing("Bearer " + accessToken).enqueue(new Callback<RedditListingObject>() {

            @Override
            public void onResponse(Call<RedditListingObject> call, Response<RedditListingObject> response) {
                String[] titleArray = new String[25];

                for (int x = 0; x < 25; x++) {
                    titleArray[x] = response.body().getData().getChildren()[x].getData().getTitle();
                }

                titleLiveData.setValue(titleArray);
            }

            @Override
            public void onFailure(Call<RedditListingObject> call, Throwable t) {
                Log.d(getClass().toString(), "requestSubRedditListing call failed");
            }
        });
    }

    public LiveData<String[]> getTitleLiveData() {
        return titleLiveData;
    }
}
