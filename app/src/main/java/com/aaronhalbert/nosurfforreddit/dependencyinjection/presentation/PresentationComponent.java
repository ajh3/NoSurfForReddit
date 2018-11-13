package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.activities.MainActivity;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {PresentationModule.class, ViewModelModule.class})
public interface PresentationComponent {
    void inject(MainActivity mainActivity);
    void inject(NoSurfWebViewFragment noSurfWebViewFragment);
}
