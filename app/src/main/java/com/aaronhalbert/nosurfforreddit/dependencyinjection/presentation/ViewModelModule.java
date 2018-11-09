package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class ViewModelModule {
    @Provides
    ViewModelFactory provideViewModelFactory(NoSurfRepository noSurfRepository) {
        return new ViewModelFactory(noSurfRepository);
    }
}
