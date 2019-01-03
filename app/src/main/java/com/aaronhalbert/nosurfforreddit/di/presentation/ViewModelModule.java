package com.aaronhalbert.nosurfforreddit.di.presentation;

import com.aaronhalbert.nosurfforreddit.data.repository.network.Repository;
import com.aaronhalbert.nosurfforreddit.utils.ViewModelFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class ViewModelModule {
    @Provides
    ViewModelFactory provideViewModelFactory(Repository repository) {
        return new ViewModelFactory(repository);
    }
}
