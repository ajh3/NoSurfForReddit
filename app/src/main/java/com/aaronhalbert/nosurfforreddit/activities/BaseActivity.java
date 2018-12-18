package com.aaronhalbert.nosurfforreddit.activities;

import android.os.Bundle;
import android.os.StrictMode;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String INJECTION_ALREADY_PERFORMED = "Injection already performed on this activity";

    private boolean isInjectorUsed;

    // region lifecycle methods --------------------------------------------------------------------

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setupStrictMode();
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region dagger -------------------------------------------------------------------------------

    @UiThread
    PresentationComponent getPresentationComponent() {
        if (isInjectorUsed) {
            throw new RuntimeException(INJECTION_ALREADY_PERFORMED);
        }

        isInjectorUsed = true;
        
        return getApplicationComponent()
                .newPresentationComponent(new PresentationModule(this), new ViewModelModule());
    }

    private ApplicationComponent getApplicationComponent() {
        return ((NoSurfApplication) getApplication()).getApplicationComponent();
    }

    // endregion dagger ----------------------------------------------------------------------------

    //detects all violations on main thread, logs to Logcat, and flashes red border if DEBUG build
    @SuppressWarnings("unused")
    private void setupStrictMode() {
        StrictMode.ThreadPolicy.Builder builder=
                new StrictMode.ThreadPolicy.Builder()
                        .detectAll() //detect all suspected violations
                        .penaltyLog(); //log detected violations to Logcat at d level

        if (BuildConfig.DEBUG) {
            builder.penaltyFlashScreen();
        }

        StrictMode.setThreadPolicy(builder.build());
    }
}
