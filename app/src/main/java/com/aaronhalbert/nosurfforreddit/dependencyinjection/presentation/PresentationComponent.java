package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.activities.MainActivity;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.NetworkingModule;
import com.aaronhalbert.nosurfforreddit.fragments.ContainerFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.PostFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {PresentationModule.class, ViewModelModule.class})
public interface PresentationComponent {
    void inject(MainActivity mainActivity);
    void inject(ContainerFragment containerFragment);
    void inject(NoSurfWebViewFragment noSurfWebViewFragment);
    void inject(PostFragment postFragment);
}
