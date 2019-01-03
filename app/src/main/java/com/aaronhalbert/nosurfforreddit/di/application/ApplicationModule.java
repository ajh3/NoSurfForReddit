package com.aaronhalbert.nosurfforreddit.di.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aaronhalbert.nosurfforreddit.data.network.auth.NoSurfAuthenticator;
import com.aaronhalbert.nosurfforreddit.data.settings.PreferenceSettingsStore;
import com.aaronhalbert.nosurfforreddit.data.network.auth.PreferenceTokenStore;
import com.aaronhalbert.nosurfforreddit.data.network.RepoUtils;
import com.aaronhalbert.nosurfforreddit.data.network.Repository;
import com.aaronhalbert.nosurfforreddit.data.network.auth.RetrofitAuthenticationInterface;
import com.aaronhalbert.nosurfforreddit.data.network.RetrofitContentInterface;
import com.aaronhalbert.nosurfforreddit.data.settings.SettingsStore;
import com.aaronhalbert.nosurfforreddit.data.network.auth.TokenStore;
import com.aaronhalbert.nosurfforreddit.data.room.ClickedPostIdRoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private final Application application;

    public ApplicationModule(Application application) {
         this.application = application;
     }

    @Singleton
    @Provides
    Repository provideNoSurfRepository(RetrofitContentInterface ri,
                                       ClickedPostIdRoomDatabase db,
                                       ExecutorService executor,
                                       NoSurfAuthenticator authenticator,
                                       RepoUtils repoUtils) {
        return new Repository(ri, db, executor, authenticator, repoUtils);
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
        return application.getSharedPreferences(application.getPackageName() + "oauth", Context.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    @Named("defaultSharedPrefs")
    SharedPreferences provideDefaultSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Singleton
    @Provides
    SettingsStore provideSettingsStore(@Named("defaultSharedPrefs") SharedPreferences preferences) {
        return new PreferenceSettingsStore(preferences);
    }

    @Singleton
    @Provides
    TokenStore provideTokenStore(@Named("oAuthSharedPrefs") SharedPreferences preferences) {
        return new PreferenceTokenStore(preferences);
    }

    @Singleton
    @Provides
    ClickedPostIdRoomDatabase provideClickedPostIdRoomDatabase() {
        return ClickedPostIdRoomDatabase.getDatabase(application);
    }

    @Singleton
    @Provides
    ExecutorService provideExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

    @Singleton
    @Provides
    NoSurfAuthenticator provideNoSurfAuthenticator(RetrofitAuthenticationInterface ri,
                                                   TokenStore tokenStore) {
        return new NoSurfAuthenticator(application, ri, tokenStore);
    }

    @Singleton
    @Provides
    RepoUtils provideRepoUtils() {
        return new RepoUtils();
    }
}