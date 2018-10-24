package com.aaronhalbert.nosurfforreddit.dependencyinjection.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.aaronhalbert.nosurfforreddit.Constants;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;
import com.aaronhalbert.nosurfforreddit.network.RateLimitInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApplicationModule {
     private final Application application;

     public ApplicationModule(Application application) {
         this.application = application;
     }

    // @Provides methods go here
    @Singleton
    @Provides
    NoSurfRepository provideNoSurfRepository(Application application, Retrofit retrofit, SharedPreferences preferences) {
        return new NoSurfRepository(application, retrofit, preferences);
    }

    @Singleton
    @Provides
    Application provideApplication() {
        return application;
    }

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences() {
        return application.getSharedPreferences(application.getPackageName() + "oauth", application.MODE_PRIVATE);
    }
}
