package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.reddit.RedditAppOnlyOAuthTokenObject;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NoSurfRepository {
    private static NoSurfRepository repositoryInstance = null;

    private static final String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private static final String DEVICE_ID = "DO_NOT_TRACK_THIS_DEVICE";
    private static final String OAUTH_BASE_URL = "https://www.reddit.com/api/v1/access_token";
    private static final String API_BASE_URL = "https://oauth.reddit.com/";
    //private static final String CLIENT_ID = "jPF59UF5MbMkWg";

    private String accessToken;

    private MutableLiveData<Listing> listingLiveData = new MutableLiveData<Listing>();

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
        ri.requestAppOnlyOAuthToken(OAUTH_BASE_URL, GRANT_TYPE, DEVICE_ID).enqueue(new Callback<RedditAppOnlyOAuthTokenObject>() {
            @Override
            public void onResponse(Call<RedditAppOnlyOAuthTokenObject> call, Response<RedditAppOnlyOAuthTokenObject> response) {
                accessToken = response.body().getAccess_token();
                requestSubRedditListing();
            }

            @Override
            public void onFailure(Call<RedditAppOnlyOAuthTokenObject> call, Throwable t) {
                Log.d(getClass().toString(), "Auth call failed");
            }
        });
    }

    public void requestSubRedditListing() {
        ri.requestSubRedditListing("Bearer " + accessToken).enqueue(new Callback<Listing>() {

            @Override
            public void onResponse(Call<Listing> call, Response<Listing> response) {
                /* String[] titleArray = new String[25];
                String[] thumbnailArray = new String[25];

                for (int x = 0; x < 25; x++) {
                    titleArray[x] = response.body().getData().getChildren()[x].getData().getTitle();
                    thumbnailArray[x] = response.body().getData().getChildren()[x].getData().getThumbnail();
                } */

                listingLiveData.setValue(response.body());

            }

            @Override
            public void onFailure(Call<Listing> call, Throwable t) {
                Log.d(getClass().toString(), "requestSubRedditListing call failed: " + t.toString());
            }
        });


    }

    public LiveData<Listing> getListingLiveData() {
        return listingLiveData;
    }

}
