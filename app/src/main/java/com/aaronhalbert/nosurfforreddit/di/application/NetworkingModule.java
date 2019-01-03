package com.aaronhalbert.nosurfforreddit.di.application;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.data.repository.network.auth.AuthenticatorUtils;
import com.aaronhalbert.nosurfforreddit.data.repository.network.RateLimitInterceptor;
import com.aaronhalbert.nosurfforreddit.data.repository.network.auth.RetrofitAuthenticationInterface;
import com.aaronhalbert.nosurfforreddit.data.repository.network.RetrofitContentInterface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
class NetworkingModule {
    @Singleton
    @Provides
    Retrofit provideRetrofit(OkHttpClient.Builder okHttpClientBuilder) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttpClientBuilder.build())
                .build();
    }

    @Singleton
    @Provides
    OkHttpClient.Builder provideOkHttpClientBuilder(HttpLoggingInterceptor httpLoggingInterceptor, RateLimitInterceptor rateLimitInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(rateLimitInterceptor);
    }

    @Provides
    HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
    }

    @Provides
    RateLimitInterceptor provideRateLimitInterceptor() {
        return new RateLimitInterceptor();
    }

    @Provides
    RetrofitContentInterface provideRetrofitContentInterface(Retrofit retrofit) {
        return retrofit.create(RetrofitContentInterface.class);
    }

    @Provides
    RetrofitAuthenticationInterface provideRetrofitAuthenticationInterface(Retrofit retrofit) {
        return retrofit.create(RetrofitAuthenticationInterface.class);
    }

    @Singleton
    @Provides
    AuthenticatorUtils provideAuthenticatorUtils() {
        return new AuthenticatorUtils();
    }
}
