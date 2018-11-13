package com.aaronhalbert.nosurfforreddit.activities;

import android.os.Bundle;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private boolean isInjectorUsed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // catch all uncaught exceptions and log them so users can email me their logs
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e(getClass().toString(), "Uncaught exception: ", e);
            }
        });
    }

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
