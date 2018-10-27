package com.aaronhalbert.nosurfforreddit.dependencyinjection.application;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostIdRoomDatabase;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
public class ApplicationModule {
     private final Application application;

     public ApplicationModule(Application application) {
         this.application = application;
     }

    @Singleton
    @Provides
    NoSurfRepository provideNoSurfRepository(Retrofit retrofit, @Named("oAuthSharedPrefs") SharedPreferences preferences, ClickedPostIdRoomDatabase db) {
        return new NoSurfRepository(retrofit, preferences, db);
    }

    @Singleton
    @Provides
    Application provideApplication() {
        return application;
    }

    @Singleton
    @Provides
    @Named("oAuthSharedPrefs")
        SharedPreferences provideOAuthSharedPrefs() {
        return application.getSharedPreferences(application.getPackageName() + "oauth", application.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    @Named("defaultSharedPrefs")
    SharedPreferences provideDefaultSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Singleton
    @Provides
    ClickedPostIdRoomDatabase provideReadPostIdRoomDatabase() {
        return ClickedPostIdRoomDatabase.getDatabase(application);
    }
}
