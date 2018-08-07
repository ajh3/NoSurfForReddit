package com.aaronhalbert.meteorforreddit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AllPostsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private static final String DEVICE_ID = "DO_NOT_TRACK_THIS_DEVICE";

    private static final String OAUTH_BASE_URL = "https://www.reddit.com";
    private static final String API_BASE_URL = "https://oauth.reddit.com";
    private static final String CLIENT_ID = "jPF59UF5MbMkWg";

    private String[] titleArray = new String[25];

    private String mParam1;
    private String mParam2;
    private String accessToken;
    private RecyclerView rv = null;

    HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
    OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(logging);

    // First retrofit instance for Reddit OAuth API
    // TODO Move Retrofit builders to a separate method

    private Retrofit auth = new Retrofit.Builder()
            .baseUrl(OAUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build();

    private RetrofitInterface riAuth = auth.create(RetrofitInterface.class);

    // Second retrofit instance for main Reddit API

    private Retrofit main = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build();

    private RetrofitInterface riMain = main.create(RetrofitInterface.class);







    public AllPostsFragment() {
        // Required empty public constructor
    }

    public static AllPostsFragment newInstance(String param1, String param2) {
        AllPostsFragment fragment = new AllPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(com.aaronhalbert.meteorforreddit.R.layout.fragment_all_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        rv = Objects.requireNonNull(getView()).findViewById(com.aaronhalbert.meteorforreddit.R.id.all_posts_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new PostsAdapter(titleArray));
        rv.setHasFixedSize(true);



        riAuth.requestAppOnlyOAuthToken(GRANT_TYPE, DEVICE_ID).enqueue(new Callback<AppOnlyOAuthToken>() {
            @Override
            public void onResponse(Call<AppOnlyOAuthToken> call, Response<AppOnlyOAuthToken> response) {

                Log.d(getClass().toString(),"App-only auth call succeeded with code=" + response.code() + " and has body = " + response.body());
                accessToken = response.body().getAccess_token();

                riMain.requestSubRedditListing("Bearer " + accessToken).enqueue(new Callback<RedditListingObject>() {
                    @Override
                    public void onResponse(Call<RedditListingObject> call, Response<RedditListingObject> response) {

                        Log.d(getClass().toString(),"requestSubRedditListing call succeeded with code=" + response.code() + " and has body = " + response.body());
                        for (int x = 0; x < 25; x++) {
                            titleArray[x] = response.body().getData().getChildren()[x].getData().getTitle();
                        }
                        Log.d(getClass().toString(), Arrays.toString(titleArray));
                        rv.getAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<RedditListingObject> call, Throwable t) {
                        Log.d(getClass().toString(), "requestSubRedditListing call failed");
                    }
                });
            }

            @Override
            public void onFailure(Call<AppOnlyOAuthToken> call, Throwable t) {
                Log.d(getClass().toString(), "Auth call failed");
            }
        });



    }


}
