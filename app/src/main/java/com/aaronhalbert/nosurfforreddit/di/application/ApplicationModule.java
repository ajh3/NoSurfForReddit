/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.di.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aaronhalbert.nosurfforreddit.data.remote.auth.NoSurfAuthenticator;
import com.aaronhalbert.nosurfforreddit.data.local.settings.PreferenceSettingsStore;
import com.aaronhalbert.nosurfforreddit.data.local.auth.PreferenceTokenStore;
import com.aaronhalbert.nosurfforreddit.data.remote.posts.PostsRepoUtils;
import com.aaronhalbert.nosurfforreddit.data.remote.posts.PostsRepo;
import com.aaronhalbert.nosurfforreddit.data.remote.auth.RetrofitAuthenticationInterface;
import com.aaronhalbert.nosurfforreddit.data.remote.posts.RetrofitContentInterface;
import com.aaronhalbert.nosurfforreddit.data.local.settings.SettingsStore;
import com.aaronhalbert.nosurfforreddit.data.local.auth.TokenStore;
import com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.ClickedPostIdRoomDatabase;

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
    PostsRepo providePostsRepo(RetrofitContentInterface ri,
                                      ClickedPostIdRoomDatabase db,
                                      ExecutorService executor,
                                      NoSurfAuthenticator authenticator,
                                      PostsRepoUtils postsRepoUtils) {
        return new PostsRepo(ri, db, executor, authenticator, postsRepoUtils);
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
    PostsRepoUtils providePostsRepoUtils() {
        return new PostsRepoUtils();
    }
}
