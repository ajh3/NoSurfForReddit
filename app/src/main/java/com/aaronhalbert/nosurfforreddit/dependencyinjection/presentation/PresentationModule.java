package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import androidx.fragment.app.FragmentActivity;
import dagger.Module;

@Module
public class PresentationModule {
    private final FragmentActivity fragmentActivity;

    public PresentationModule(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    // @Provides methods go here


}
