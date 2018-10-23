package com.aaronhalbert.nosurfforreddit.dependencyinjection.application;

import com.aaronhalbert.nosurfforreddit.Constants;
import com.aaronhalbert.nosurfforreddit.network.RateLimitInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkingModule {
    // @Provides methods go here

    @Singleton
    @Provides
    Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClientBuilder().build())
                .build();
    }

    @Singleton
    @Provides
    OkHttpClient.Builder getOkHttpClientBuilder() {
        return new OkHttpClient.Builder()
                .addInterceptor(getHttpLoggingInterceptor())
                .addInterceptor(new RateLimitInterceptor());
    }

    @Provides
    HttpLoggingInterceptor getHttpLoggingInterceptor() {
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
    }


}
