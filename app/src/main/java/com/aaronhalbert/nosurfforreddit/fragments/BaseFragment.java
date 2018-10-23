package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;

import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {
    private boolean isInjectorUsed;

    @UiThread
    protected PresentationComponent getPresentationComponent() {
        if (isInjectorUsed) {
            throw new RuntimeException("Injector already used");
        }
        isInjectorUsed = true;
        return getApplicationComponent()
                .newPresentationComponent(new PresentationModule(getActivity()));

    }

    private ApplicationComponent getApplicationComponent() {
        return ((NoSurfApplication) getActivity().getApplication()).getApplicationComponent();
    }
}
