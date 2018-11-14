package com.aaronhalbert.nosurfforreddit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.NoSurfApplication;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation.ViewModelModule;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String FILENAME_EXTENSION = ".txt";
    private static final String FAILED_TO_WRITE_LOGS_TO_FILE = "Failed to write logs to file";
    private static final String DEV_EMAIL = "aaron.james.halbert@gmail.com";
    private static final String EMAIL_SUBJECT = "NoSurf for reddit crash log";
    private static final String PROMPT = "Unexpected error: Send bug report?";
    private boolean isInjectorUsed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // catch all uncaught exceptions and log them so users can email me their logs
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e(getClass().toString(), "Uncaught exception: ", e);
                sendLogcatMail();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    void sendLogcatMail() {
        File outputFile = new File(Environment.getExternalStorageDirectory(),
                generateRandomAlphaNumericString() + FILENAME_EXTENSION);
        try {
            Runtime.getRuntime().exec(
                    "logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(getClass().toString(), FAILED_TO_WRITE_LOGS_TO_FILE);
        }

        Intent i = new Intent(Intent.ACTION_SENDTO);
        String to[] = {DEV_EMAIL};
        i.putExtra(Intent.EXTRA_EMAIL, to);
        i.putExtra(Intent.EXTRA_STREAM, outputFile.getAbsolutePath());
        i.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
        startActivity(Intent.createChooser(i , PROMPT));
    }

    String generateRandomAlphaNumericString() {
        return UUID.randomUUID().toString();
    }
}
