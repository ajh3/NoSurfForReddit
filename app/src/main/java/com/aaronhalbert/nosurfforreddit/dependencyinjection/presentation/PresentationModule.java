package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;

import javax.inject.Singleton;

import androidx.fragment.app.FragmentActivity;
import dagger.Module;
import dagger.Provides;

@Module
public class PresentationModule {
    private final FragmentActivity fragmentActivity;

    public PresentationModule(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    // @Provides methods go here


}
