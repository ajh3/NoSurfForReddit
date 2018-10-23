package com.aaronhalbert.nosurfforreddit.dependencyinjection.application;

import com.aaronhalbert.nosurfforreddit.activities.MainActivity;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkingModule.class})
public interface ApplicationComponent {
    // factory method returning subcomponent, to establish parent/child relationship
    public PresentationComponent newPresentationComponent(PresentationModule presentationModule, ViewModelModule viewModelModule);

    //void inject(MainActivity mainActivity);
}
