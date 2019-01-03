package com.aaronhalbert.nosurfforreddit.dependencyinjection.application;

import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkingModule.class})
public interface ApplicationComponent {
    /* factory method returning subcomponent, to establish parent/child relationship.
     *
     * Since we have a subcomponent, we don't need to expose any dependencies publicly
     * from this component; sub-components automatically have access to the entire object graph. */
    PresentationComponent newPresentationComponent(PresentationModule presentationModule,
                                                   ViewModelModule viewModelModule);
}
