package com.aaronhalbert.nosurfforreddit.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException;
import com.aaronhalbert.nosurfforreddit.repository.SettingsStore;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static com.aaronhalbert.nosurfforreddit.repository.NoSurfAuthenticator.extractCodeFromIntent;
import static com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel.NetworkErrors;

public class MainActivity extends BaseActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Error: Access denied";
    private static final String LOGIN_FAILED_ERROR_MESSAGE = "Error: Login failed";
    private static final String NETWORK_ERROR_MESSAGE = "Network error!";

    @SuppressWarnings("WeakerAccess") @Inject SettingsStore settingsStore;
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

        /* only run the splash animation on first app launch */
        if (savedInstanceState == null) {
            setupSplashAnimation();
        }

        initNavComponent();
        subscribeToNetworkErrors();
    }

    @Override
    protected void onResume() {
        super.onResume();

        settingsStore.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        settingsStore.unregisterListener(this);
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private void initPrefs() {
        PreferenceManager.setDefaultValues(getApplication(), R.xml.preferences, false);

        nightMode = settingsStore.isNightMode();
        amoledNightMode = settingsStore.isAmoledNightMode();
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
        new WebView(this); // see comment in nightModeOn()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void navUp() {
        navController.navigateUp();
    }

    private void initNavComponent() {
        navController = Navigation.findNavController(findViewById(R.id.nav_host_fragment));

        /* NavigationUI uses AppBarConfiguration to manage the "UP" button in top-left corner */
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();

        Toolbar t = findViewById(R.id.toolbar);
        setSupportActionBar(t);

        NavigationUI.setupWithNavController(t, navController, appBarConfiguration);
    }

    /* we implement the splash animation as a View inside MainActivity rather than having a
     * separate SplashActivity. The latter would be slower, and we don't want to display the
     * splash for any longer than is necessary. */
    private void setupSplashAnimation() {
        Animator refreshDrawableAnimator = AnimatorInflater.loadAnimator(this, R.animator.splash_animation);
        ImageView iv = findViewById(R.id.logo);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.web_hi_res_512));
        refreshDrawableAnimator.setTarget(iv);
        refreshDrawableAnimator.start();

        setupSplashCanceler();
    }

    /* clear the splash screen as soon as data have arrived */
    private void setupSplashCanceler() {
        viewModel.getAllPostsViewStateLiveData().observe(this, postsViewState ->
                findViewById(R.id.logo).setVisibility(View.GONE));
    }

    // endregion helper methods --------------------------------------------------------------------

    // region observers/listeners ------------------------------------------------------------------

    private void subscribeToNetworkErrors() {
        viewModel.getNetworkErrorsLiveData().observe(this, networkErrorsEvent -> {
            NetworkErrors n = networkErrorsEvent.getContentIfNotHandled();

            if (n != null) {
                Toast.makeText(this, NETWORK_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        nightMode = settingsStore.isNightMode();

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
