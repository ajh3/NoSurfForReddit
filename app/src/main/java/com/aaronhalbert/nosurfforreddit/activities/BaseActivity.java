package com.aaronhalbert.nosurfforreddit.activities;

import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private boolean isInjectorUsed;

    @UiThread
    PresentationComponent getPresentationComponent() {
        if (isInjectorUsed) {
            throw new RuntimeException("Injection already performed on this activity");
        }

        isInjectorUsed = true;
        
        return getApplicationComponent()
                .newPresentationComponent(new PresentationModule(this), new ViewModelModule());
    }

    private ApplicationComponent getApplicationComponent() {
        return ((NoSurfApplication) getApplication()).getApplicationComponent();
    }
}
