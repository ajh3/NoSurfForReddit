package com.aaronhalbert.nosurfforreddit;

import android.os.Bundle;
import android.os.StrictMode;

import com.aaronhalbert.nosurfforreddit.di.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.di.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.di.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.di.presentation.ViewModelModule;

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

    @SuppressWarnings("UnusedReturnValue")
    @UiThread
    public PresentationComponent getPresentationComponent() {
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
