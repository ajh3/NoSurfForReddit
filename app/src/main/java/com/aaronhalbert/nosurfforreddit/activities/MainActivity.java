package com.aaronhalbert.nosurfforreddit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException;
import com.aaronhalbert.nosurfforreddit.fragments.AboutFragment;
import com.aaronhalbert.nosurfforreddit.fragments.LinkPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfPreferenceFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.SelfPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;
import com.aaronhalbert.nosurfforreddit.network.Repository;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.webview.LaunchWebViewParams;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import static com.aaronhalbert.nosurfforreddit.network.NoSurfAuthenticator.buildAuthUrl;
import static com.aaronhalbert.nosurfforreddit.network.NoSurfAuthenticator.extractCodeFromIntent;

public class MainActivity extends BaseActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG_WEBVIEW_LOGIN_FRAGMENT = "webviewLoginFragmentTag";
    private static final String TAG_VIEW_PAGER_FRAGMENT = "viewPagerFragmentTag";
    private static final String KEY_NIGHT_MODE = "nightMode";
    private static final String KEY_AMOLED_NIGHT_MODE = "amoledNightMode";
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Error: Access denied";
    private static final String LOGIN_FAILED_ERROR_MESSAGE = "Error: Login failed";
    private static final String NETWORK_ERROR_MESSAGE = "Network error!";

    @SuppressWarnings("WeakerAccess") @Inject @Named("defaultSharedPrefs") SharedPreferences preferences;
    @SuppressWarnings("WeakerAccess") @Inject ViewModelFactory viewModelFactory;
    private MainActivityViewModel viewModel;
    private FragmentManager fm;

    private boolean nightMode;
    private boolean amoledNightMode;
    private boolean externalBrowser;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        initPrefs();
        initNightMode(); // call before super to avoid MainActivity recreating itself
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // only necessary to specify the factory the first time here, subsequent calls to
        // ViewModelProviders.of for this activity will get the right ViewModel without specifying
        // the factory
        viewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainActivityViewModel.class);

        // LiveData event subscriptions
        subscribeToNetworkErrors();
        subscribeToRecyclerViewClickEvents();
        subscribeToPostFragmentClickEvents();
        subscribeToLoginFragmentClickEvents();
        subscribeToViewPagerFragmentClickEvents();

        fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(TAG_VIEW_PAGER_FRAGMENT) == null) {
            fm.beginTransaction()
                    .add(R.id.main_activity_frame_layout,
                            ViewPagerFragment.newInstance(),
                            TAG_VIEW_PAGER_FRAGMENT)
                    .commit();
        }
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
        externalBrowser = preferences.getBoolean(KEY_EXTERNAL_BROWSER, false);
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

        if (amoledNightMode) {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAmoledNightBg));
        } else {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorNightBg));
        }
    }

    private void nightModeOff() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    // endregion helper methods --------------------------------------------------------------------

    // region login/logout -------------------------------------------------------------------------

    public void login() {
        launchWebView(new LaunchWebViewParams(
                buildAuthUrl(),
                TAG_WEBVIEW_LOGIN_FRAGMENT,
                true));

        /* onNewIntent captures the result of this login attempt, and is responsible for calling
         * viewModel.logUserIn() if it's successful */
    }

    public void logout() {
        viewModel.logUserOut();
    }

    // endregion login/logout ----------------------------------------------------------------------

    // region observers/listeners ------------------------------------------------------------------

    /* MainActivity is in charge of all navigation. It uses the listeners below
     * to listen/react to nav events propagated via LiveData through the ViewModel */

    //TODO: is there a way to cleanly throw exceptions from the Repository up to the view layer
    // and show Toasts that way, so we don't need this observer?
    private void subscribeToNetworkErrors() {
        Context context = this;

        viewModel.getNetworkErrorsLiveData().observe(this, networkErrorsEvent -> {
            Repository.NetworkErrors n = networkErrorsEvent.getContentIfNotHandled();

            if (n != null) {
                Toast.makeText(context, NETWORK_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subscribeToRecyclerViewClickEvents() {
        viewModel.getRecyclerViewClickEventsLiveData().observe(this, clickEvent -> {
            Boolean b = clickEvent.getContentIfNotHandled();

            if (b != null) {
                launchPost();
            }
        });
    }

    private void subscribeToPostFragmentClickEvents() {
        viewModel.getPostFragmentClickEventsLiveData().observe(this, clickEvent -> {
            LaunchWebViewParams l = clickEvent.getContentIfNotHandled();

            if (l != null) {
                if (externalBrowser) {
                    launchExternalBrowser(Uri.parse(l.url));
                } else {
                    launchWebView(l);
                }
            }
        });
    }

    private void subscribeToLoginFragmentClickEvents() {
        viewModel.getLoginFragmentClickEventsLiveData().observe(this, clickEvent -> {
            Boolean b = clickEvent.getContentIfNotHandled();

            if (b != null) {
                login();
            }
        });
    }

    private void subscribeToViewPagerFragmentClickEvents() {
        viewModel.getViewPagerFragmentClickEventsLiveData().observe(this, clickEvent -> {
            ViewPagerFragment.ViewPagerFragmentNavigationEvents v = clickEvent.getContentIfNotHandled();

            if (v != null) {
                switch (v) {
                    case VIEW_PAGER_FRAGMENT_LOGIN_EVENT:
                        login();
                        break;
                    case VIEW_PAGER_FRAGMENT_LOGOUT_EVENT:
                        logout();
                        break;
                    case VIEW_PAGER_FRAGMENT_LAUNCH_PREFS_EVENT:
                        launchPreferencesScreen();
                        break;
                    case VIEW_PAGER_FRAGMENT_LAUNCH_ABOUT_EVENT:
                        launchAboutScreen();
                        break;
                    default:
                        break;
                }
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

    // captures result from Reddit login page using custom redirect URI nosurfforreddit://oauth
    // intent filter is configured in manifest
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String code;
            Fragment loginFragment = fm.findFragmentByTag(TAG_WEBVIEW_LOGIN_FRAGMENT);

            if (loginFragment != null) {
                fm.beginTransaction().remove(loginFragment).commit();
            }

            try {
                code = extractCodeFromIntent(intent);
            } catch (NoSurfAccessDeniedLoginException e) {
                Log.e(getClass().toString(), ACCESS_DENIED_ERROR_MESSAGE, e);
                Toast.makeText(this, ACCESS_DENIED_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                return;
            } catch (NoSurfLoginException e) {
                Log.e(getClass().toString(), LOGIN_FAILED_ERROR_MESSAGE, e);
                Toast.makeText(this, LOGIN_FAILED_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                return;
            }

            viewModel.logUserIn(code);
        }
    }

    // endregion observers/listeners ---------------------------------------------------------------

    // region app navigation -----------------------------------------------------------------------

    public void launchWebView(LaunchWebViewParams launchWebViewParams) {
        String url = launchWebViewParams.url;
        String tag = launchWebViewParams.tag;
        boolean doAnimation = launchWebViewParams.doAnimation;

        FragmentTransaction ft = fm.beginTransaction();

        if (doAnimation) {
            ft.setCustomAnimations(
                    R.anim.push_up_in,
                    R.anim.push_up_out,
                    R.anim.push_down_in,
                    R.anim.push_down_out);
        }

        ft.add(R.id.main_activity_frame_layout, NoSurfWebViewFragment.newInstance(url), tag)
                .addToBackStack(null)
                .commit();
    }

    private void launchExternalBrowser(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void launchPost() {
        Fragment f;

        if (viewModel.getLastClickedPostMetadata().lastClickedPostIsSelf) {
            f = SelfPostFragment.newInstance();
        } else {
            f = LinkPostFragment.newInstance();
        }

        fm.beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right)
                .replace(R.id.main_activity_frame_layout, f)
                .addToBackStack(null)
                .commit();
    }

    private void launchPreferencesScreen() {
        fm.beginTransaction()
                .setCustomAnimations(
                        R.anim.push_up_in,
                        R.anim.push_up_out,
                        R.anim.push_down_in,
                        R.anim.push_down_out)
                .replace(R.id.main_activity_frame_layout, NoSurfPreferenceFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    private void launchAboutScreen() {
        fm.beginTransaction()
                .setCustomAnimations(
                        R.anim.push_up_in,
                        R.anim.push_up_out,
                        R.anim.push_down_in,
                        R.anim.push_down_out)
                .replace(R.id.main_activity_frame_layout, AboutFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    // endregion app navigation --------------------------------------------------------------------
}
