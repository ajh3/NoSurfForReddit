package com.aaronhalbert.nosurfforreddit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException;
import com.aaronhalbert.nosurfforreddit.network.Repository;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import static com.aaronhalbert.nosurfforreddit.network.NoSurfAuthenticator.extractCodeFromIntent;

public class MainActivity extends BaseActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_NIGHT_MODE = "nightMode";
    private static final String KEY_AMOLED_NIGHT_MODE = "amoledNightMode";
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Error: Access denied";
    private static final String LOGIN_FAILED_ERROR_MESSAGE = "Error: Login failed";
    private static final String NETWORK_ERROR_MESSAGE = "Network error!";

    @SuppressWarnings("WeakerAccess") @Inject @Named("defaultSharedPrefs") SharedPreferences preferences;
    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    private MainActivityViewModel viewModel;
    private NavController navController;

    private boolean nightMode;
    private boolean amoledNightMode;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        initPrefs();
        initNightMode(); // call before super to avoid MainActivity recreating itself
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* only necessary to specify the factory the first time here, subsequent calls to
         * ViewModelProviders.of for MainActivityViewModel will get the right ViewModel without
         * specifying the factory */
        viewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainActivityViewModel.class);

        navController = Navigation.findNavController(findViewById(R.id.nav_host_fragment));

        subscribeToNetworkErrors();
    }

    @Override
    protected void onResume() {
        super.onResume();

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void initPrefs() {
        PreferenceManager.setDefaultValues(getApplication(), R.xml.preferences, false);
        nightMode = preferences.getBoolean(KEY_NIGHT_MODE, true);
        amoledNightMode = preferences.getBoolean(KEY_AMOLED_NIGHT_MODE, false);
    }

    private void initNightMode() {
        if (nightMode) {
            nightModeOn();
        } else {
            nightModeOff();
        }
    }

    private void nightModeOn() {
        new WebView(this); //DayNight fix: https://stackoverflow.com/questions/44035654/broken-colors-in-daynight-theme-after-loading-admob-firebase-ad
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        View v = getWindow().getDecorView();

        if (amoledNightMode) {
            v.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAmoledNightBg));
        } else {
            v.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNightBg));
        }
    }

    private void nightModeOff() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void navUp() {
        Navigation.findNavController(findViewById(R.id.nav_host_fragment))
                .navigateUp();
    }

    // endregion helper methods --------------------------------------------------------------------

    // region observers/listeners ------------------------------------------------------------------

    /* TODO: is there a way to cleanly throw exceptions from the Repository up to the view layer
     * and show Toasts that way, so we don't need this observer? */
    private void subscribeToNetworkErrors() {
        Context context = this;

        viewModel.getNetworkErrorsLiveData().observe(this, networkErrorsEvent -> {
            Repository.NetworkErrors n = networkErrorsEvent.getContentIfNotHandled();

            if (n != null) {
                Toast.makeText(context, NETWORK_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        nightMode = sharedPreferences.getBoolean(KEY_NIGHT_MODE, true);

        if (nightMode) {
            nightModeOn();
            recreate();
        } else {
            nightModeOff();
            recreate();
        }
    }

    /* captures result from Reddit login page using custom redirect URI nosurfforreddit://oauth .
     * Intent filter is configured in manifest. */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String code;

            try {
                code = extractCodeFromIntent(intent);
            } catch (NoSurfAccessDeniedLoginException e) {
                Log.e(getClass().toString(), ACCESS_DENIED_ERROR_MESSAGE, e);
                Toast.makeText(this, ACCESS_DENIED_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                navUp();
                return;
            } catch (NoSurfLoginException e) {
                Log.e(getClass().toString(), LOGIN_FAILED_ERROR_MESSAGE, e);
                Toast.makeText(this, LOGIN_FAILED_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                navUp();
                return;
            }

            viewModel.logUserIn(code);
            navUp();
        }
    }

    // endregion observers/listeners ---------------------------------------------------------------
}
