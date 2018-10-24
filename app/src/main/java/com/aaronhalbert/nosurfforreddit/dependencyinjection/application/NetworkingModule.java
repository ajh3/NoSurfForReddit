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
    Retrofit getRetrofit(OkHttpClient.Builder okHttpClientBuilder) {
        return new Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientBuilder.build())
                .build();
    }

    @Singleton
    @Provides
    OkHttpClient.Builder getOkHttpClientBuilder(HttpLoggingInterceptor httpLoggingInterceptor, RateLimitInterceptor rateLimitInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(rateLimitInterceptor);
    }

    @Provides
    HttpLoggingInterceptor getHttpLoggingInterceptor() {
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
    }

    @Provides
    RateLimitInterceptor getRateLimitInterceptor() {
        return new RateLimitInterceptor();
    }


}
