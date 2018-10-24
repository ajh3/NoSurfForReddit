package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class ViewModelModule {
    @Provides
    ViewModelFactory getViewModelFactory(NoSurfRepository noSurfRepository) {
        return new ViewModelFactory(noSurfRepository);
    }
}
