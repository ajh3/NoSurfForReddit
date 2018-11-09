package com.aaronhalbert.nosurfforreddit.activities;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
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
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;

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
    private boolean nightMode;
    private boolean amoledNightMode;
    private boolean externalBrowser;
    private FragmentManager fm;

    // region lifecycle methods --------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);
        setupStrictMode();
        initPrefs();
        initNightMode();
        setContentView(R.layout.activity_main);

        // only necessary to specify the factory the first time here, subsequent calls to
        // ViewModelProviders.of for this activity will get the right vm without specifying
        // the factory
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NoSurfViewModel.class);

        subscribeToNetworkErrors();
        subscribeToPostsFragmentClicks();
        subscribeToPostFragmentClicks();
        subscribeToLoginFragmentClicks();
        subscribeToViewPagerFragmentClicks();

        fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(TAG_VIEW_PAGER_FRAGMENT) == null) {
            fm.beginTransaction()
                    .add(R.id.main_activity_frame_layout,
                            ViewPagerFragment.newInstance(),
                            TAG_VIEW_PAGER_FRAGMENT)
                    .commit();
        }
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region login/logout -------------------------------------------------------------------------

    public void login() {
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

        launchWebView(new LaunchWebViewParams(authUrl, TAG_WEBVIEW_LOGIN_FRAGMENT, true));
    }

    public void logout() {
        clearCookies();
        viewModel.logUserOut();
    }

    // endregion login/logout ----------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private String generateRandomAlphaNumericString() {
        return UUID.randomUUID().toString();
    }

    //detects all violations on main thread, logs to Logcat, and flashes red border if DEBUG build
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

    private void initPrefs() {
        PreferenceManager.setDefaultValues(getApplication(), R.xml.preferences, false);
        preferences.registerOnSharedPreferenceChangeListener(this);
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

    // calling nightModeOn() or nightModeOff() in onCreate() seems to cause the
    // activity to be recreated after onCreate() finishes
    // unclear if it's expected behavior or a bug
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

    // endregion helper methods --------------------------------------------------------------------

    // region observers/listeners ------------------------------------------------------------------

    private void subscribeToNetworkErrors() {
        viewModel.getNetworkErrorsLiveData().observe(this, networkErrorsEvent -> {
            NoSurfRepository.NetworkErrors n = networkErrorsEvent.getContentIfNotHandled();

            if (n != null) {
                Toast.makeText(getApplicationContext(), NETWORK_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subscribeToPostsFragmentClicks() {
        viewModel.getPostsFragmentClickEventsLiveData().observe(this, clickEvent -> {
            Boolean b = clickEvent.getContentIfNotHandled();

            if (b != null) {
                launchPost();
            }
        });
    }

    private void subscribeToPostFragmentClicks() {
        viewModel.getPostFragmentClickEventsLiveData().observe(this, clickEvent -> {
            LaunchWebViewParams l = clickEvent.getContentIfNotHandled();

            if (l != null) {
                if (externalBrowser) {
                    launchExternalBrowser(Uri.parse(l.getUrl()));
                } else {
                    launchWebView(l);
                }
            }
        });
    }

    private void subscribeToLoginFragmentClicks() {
        viewModel.getLoginFragmentClickEventsLiveData().observe(this, clickEvent -> {
            Boolean b = clickEvent.getContentIfNotHandled();

            if (b != null) {
                login();
            }
        });
    }

    private void subscribeToViewPagerFragmentClicks() {
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
        if (nightMode) {
            nightModeOn();
            recreate();
        } else {
            nightModeOff();
            recreate();
        }
    }

    // captures result from Reddit login page
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Fragment loginFragment = fm.findFragmentByTag(TAG_WEBVIEW_LOGIN_FRAGMENT);

            if (loginFragment != null) {
                fm.beginTransaction().remove(loginFragment).commit();
            }

            Uri uri = intent.getData();
            String error;
            String code;

            //TODO: is there a good way to avoid this null check?
            //TODO: should this be using try/catch, or is it not "exceptional" enough? I think the latter
            if (uri != null) {
                error = uri.getQueryParameter(ERROR);
                code = uri.getQueryParameter(CODE);
            } else {
                error = "";
                code = "";
            }

            if (ACCESS_DENIED_ERROR_CODE.equals(error)) {
                Toast.makeText(this, ACCESS_DENIED_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
            } else if ("".equals(error)) {
                Toast.makeText(this, LOGIN_FAILED_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.fetchUserOAuthTokenASync(code);
            }
        }
    }

    // endregion observers/listeners ---------------------------------------------------------------

    // region app navigation -----------------------------------------------------------------------

    public void launchWebView(LaunchWebViewParams launchWebViewParams) {
        String url = launchWebViewParams.getUrl();
        String tag = launchWebViewParams.getTag();
        boolean doAnimation = launchWebViewParams.isDoAnimation();

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

    public void launchExternalBrowser(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void launchPost() {
        Fragment f;

        if (viewModel.getLastClickedPostMetadata().isLastClickedPostIsSelf()) {
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

    public void launchPreferencesScreen() {
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

    public void launchAboutScreen() {
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
