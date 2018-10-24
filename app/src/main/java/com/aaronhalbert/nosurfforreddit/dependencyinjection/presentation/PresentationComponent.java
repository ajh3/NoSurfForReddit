package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.activities.MainActivity;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.NetworkingModule;
import com.aaronhalbert.nosurfforreddit.fragments.ContainerFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {PresentationModule.class, ViewModelModule.class})
public interface PresentationComponent {
    // void methods go here
    void inject(MainActivity mainActivity);

    void inject(ContainerFragment containerFragment);


}
