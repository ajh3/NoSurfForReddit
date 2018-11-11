package com.aaronhalbert.nosurfforreddit.fragments;

import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;

import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

abstract class BaseFragment extends Fragment {
    private boolean isInjectorUsed;

    @UiThread
    PresentationComponent getPresentationComponent() {
        if (isInjectorUsed) {
            throw new RuntimeException("Injection already performed on this fragment");
        }

        isInjectorUsed = true;

        return getApplicationComponent()
                .newPresentationComponent(new PresentationModule(getActivity()), new ViewModelModule());
    }

    private ApplicationComponent getApplicationComponent() {
        return ((NoSurfApplication) getActivity().getApplication()).getApplicationComponent();
    }
}
