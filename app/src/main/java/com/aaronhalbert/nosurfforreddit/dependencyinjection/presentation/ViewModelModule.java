package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.network.Repository;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModelFactory;
import com.aaronhalbert.nosurfforreddit.viewmodel.SplashActivityViewModelFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class ViewModelModule {
    @Provides
    MainActivityViewModelFactory provideMainActivityViewModelFactory(Repository repository) {
        return new MainActivityViewModelFactory(repository);
    }

    @Provides
    SplashActivityViewModelFactory provideSplashActivityViewModelFactory(Repository repository) {
        return new SplashActivityViewModelFactory(repository);
    }
}
