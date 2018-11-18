package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.network.Repository;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class ViewModelModule {
    @Provides
    ViewModelFactory provideViewModelFactory(Repository repository) {
        return new ViewModelFactory(repository);
    }
}
