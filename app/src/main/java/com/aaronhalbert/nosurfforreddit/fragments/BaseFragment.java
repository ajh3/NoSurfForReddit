package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;

import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

abstract class BaseFragment extends Fragment {
    private static final String INJECTION_ALREADY_PERFORMED = "Injection already performed on this activity";

    private boolean isInjectorUsed;

    @UiThread
    PresentationComponent getPresentationComponent() {
        if (isInjectorUsed) {
            throw new RuntimeException(INJECTION_ALREADY_PERFORMED);
        }

        isInjectorUsed = true;

        return getApplicationComponent()
                .newPresentationComponent(new PresentationModule(getActivity()), new ViewModelModule());
    }

    private ApplicationComponent getApplicationComponent() {
        return ((NoSurfApplication) getActivity().getApplication()).getApplicationComponent();
    }
}
