package com.aaronhalbert.nosurfforreddit.dependencyinjection.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;
import com.aaronhalbert.nosurfforreddit.network.RateLimitInterceptor;
import com.aaronhalbert.nosurfforreddit.room.ReadPostIdRoomDatabase;

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
    NoSurfRepository provideNoSurfRepository(Retrofit retrofit, SharedPreferences preferences, ReadPostIdRoomDatabase db) {
        return new NoSurfRepository(retrofit, preferences, db);
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

    @Singleton
    @Provides
    ReadPostIdRoomDatabase provideReadPostIdRoomDatabase() {
        return ReadPostIdRoomDatabase.getDatabase(application);
    }
}
