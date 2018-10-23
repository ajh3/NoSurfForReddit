package com.aaronhalbert.nosurfforreddit.activities;

import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    private boolean isInjectorUsed;

    @UiThread
    protected PresentationComponent getPresentationComponent() {
        if (isInjectorUsed) {
            throw new RuntimeException("Injector already used");
        }
        isInjectorUsed = true;
        return getApplicationComponent()
                .newPresentationComponent(new PresentationModule(this), new ViewModelModule());

    }

    private ApplicationComponent getApplicationComponent() {
        return ((NoSurfApplication) getApplication()).getApplicationComponent();
    }
}
