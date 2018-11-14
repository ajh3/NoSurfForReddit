package com.aaronhalbert.nosurfforreddit.activities;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException;
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException;
import com.aaronhalbert.nosurfforreddit.network.Repository;
import com.aaronhalbert.nosurfforreddit.webview.LaunchWebViewParams;
import com.aaronhalbert.nosurfforreddit.viewmodel.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.fragments.AboutFragment;
import com.aaronhalbert.nosurfforreddit.fragments.LinkPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfPreferenceFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.SelfPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

public class MainActivity extends BaseActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG_WEBVIEW_LOGIN_FRAGMENT = "webviewLoginFragmentTag";
    private static final String TAG_VIEW_PAGER_FRAGMENT = "viewPagerFragmentTag";
    private static final String KEY_NIGHT_MODE = "nightMode";
    private static final String KEY_AMOLED_NIGHT_MODE = "amoledNightMode";
    private static final String KEY_EXTERNAL_BROWSER = "externalBrowser";
    private static final String CLIENT_ID = "jPF59UF5MbMkWg";
    private static final String RESPONSE_TYPE = "code";
    private static final String REDIRECT_URI = "nosurfforreddit://oauth";
    private static final String DURATION = "permanent";
    private static final String SCOPE = "identity mysubreddits read";
    private static final String AUTH_URL_BASE = "https://www.reddit.com/api/v1/authorize.compact?client_id=";
    private static final String AUTH_URL_RESPONSE_TYPE = "&response_type=";
    private static final String AUTH_URL_STATE = "&state=";
    private static final String AUTH_URL_REDIRECT_URI = "&redirect_uri=";
    private static final String AUTH_URL_DURATION = "&duration=";
    private static final String AUTH_URL_SCOPE = "&scope=";
    private static final String ERROR = "error";
    private static final String CODE = "code";
    private static final String ACCESS_DENIED_ERROR_CODE = "access_denied";
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Error: Access denied";
    private static final String LOGIN_FAILED_ERROR_MESSAGE = "Error: Login failed";
    private static final String NETWORK_ERROR_MESSAGE = "Network error!";

    @SuppressWarnings("WeakerAccess")
    @Inject @Named("defaultSharedPrefs") SharedPreferences preferences;

    @SuppressWarnings("WeakerAccess")
    @Inject ViewModelFactory viewModelFactory;
    private NoSurfViewModel viewModel;
    private FragmentManager fm;

    private boolean nightMode;
    private boolean amoledNightMode;
    private boolean externalBrowser;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // initialization
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        initPrefs();
        initNightMode();
        setContentView(R.layout.activity_main);

        // only necessary to specify the factory the first time here, subsequent calls to
        // ViewModelProviders.of for this activity will get the right ViewModel without specifying
        // the factory
        viewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(NoSurfViewModel.class);

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

    // region login/logout -------------------------------------------------------------------------

    private void login() {
        final String authUrl
                = AUTH_URL_BASE
                + CLIENT_ID
                + AUTH_URL_RESPONSE_TYPE
                + RESPONSE_TYPE
                + AUTH_URL_STATE
                + generateRandomAlphaNumericString()
                + AUTH_URL_REDIRECT_URI
                + REDIRECT_URI
                + AUTH_URL_DURATION
                + DURATION
                + AUTH_URL_SCOPE
                + SCOPE;

        launchWebView(new LaunchWebViewParams(
                authUrl,
                TAG_WEBVIEW_LOGIN_FRAGMENT,
                true));
    }

    /* there is no viewModel.logUserIn() method, see Repository.fetchUserOAuthTokenASync()
     * for the login routine */

    private void logout() {
        clearCookies();
        viewModel.logUserOut();
    }

    // endregion login/logout ----------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    //detects all violations on main thread, logs to Logcat, and flashes red border if DEBUG build

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

    /* TODO: calling nightModeOn() or nightModeOff() in onCreate() seems to cause the
     * activity to be recreated after onCreate() finishes
     * unclear if it's expected behavior or a bug */
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

    // clears cookies in the app's WebView
    private void clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(getApplication());
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    private String extractCodeFromIntent(Intent i) throws NoSurfLoginException {
        Uri uri;
        String error;
        String code;

        if (i.getData() != null) {
            uri = i.getData();
        } else {
            throw new NoSurfLoginException();
        }

        if (uri != null) {
            error = uri.getQueryParameter(ERROR);
            code = uri.getQueryParameter(CODE);
        } else {
            throw new NoSurfLoginException();
        }

        if (ACCESS_DENIED_ERROR_CODE.equals(error)) {
            throw new NoSurfAccessDeniedLoginException();
        } else if ("".equals(error)) {
            throw new NoSurfLoginException();
        }

        if (code != null && !"".equals(code)) {
            return code;
        } else {
            throw new NoSurfLoginException();
        }
    }

    String generateRandomAlphaNumericString() {
        return UUID.randomUUID().toString();
    }

    // endregion helper methods --------------------------------------------------------------------

    // region observers/listeners ------------------------------------------------------------------

    /* MainActivity is in charge of all navigation. It uses the listeners below
     * to listen/react to nav events propagated via LiveData through the ViewModel */

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

            viewModel.fetchUserOAuthTokenASync(code);
        }
    }

    // endregion observers/listeners ---------------------------------------------------------------

    // region app navigation -----------------------------------------------------------------------

    private void launchWebView(LaunchWebViewParams launchWebViewParams) {
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
