package com.aaronhalbert.nosurfforreddit.activities;

import androidx.appcompat.app.AppCompatDelegate;
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
import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.ViewModelFactory;
import com.aaronhalbert.nosurfforreddit.fragments.AboutFragment;
import com.aaronhalbert.nosurfforreddit.fragments.LinkPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.PostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.LoginFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfPreferenceFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.PostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.SelfPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

public class MainActivity extends BaseActivity implements
        PostFragment.PostFragmentInteractionListener,
        PostsFragment.PostsFragmentInteractionListener,
        LoginFragment.LoginFragmentInteractionListener,
        ViewPagerFragment.ViewPagerFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG_WEBVIEW_LOGIN_FRAGMENT = "webviewLoginFragmentTag";
    private static final String TAG_VIEW_PAGER_FRAGMENT = "viewPagerFragmentTag";
    private static final String KEY_DARK_MODE = "darkMode";
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

    @Inject @Named("defaultSharedPrefs") SharedPreferences preferences;
    @Inject ViewModelFactory viewModelFactory;
    
    private NoSurfViewModel viewModel;
    private boolean darkMode;
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

        fm = getSupportFragmentManager();

        // only necessary to specify the factory the first time here, subsequent calls to
        // ViewModelProviders.of for this activity will get the right vm without specifying
        // the factory
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NoSurfViewModel.class);

        if (fm.findFragmentByTag(TAG_VIEW_PAGER_FRAGMENT) == null) {
            viewModel.initApp();

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

        launchWebView(authUrl, TAG_WEBVIEW_LOGIN_FRAGMENT, true);
    }

    public void logout() {
        clearCookies();
        viewModel.logUserOut();
    }

    // endregion login/logout ----------------------------------------------------------------------

    // region app navigation -----------------------------------------------------------------------

    public void launchWebView(String url, String tag, boolean doAnimation) {
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
        darkMode = preferences.getBoolean(KEY_DARK_MODE, true);
    }

    private void initNightMode() {
        if (darkMode) {
            new WebView(this); //DayNight fix: https://stackoverflow.com/questions/44035654/broken-colors-in-daynight-theme-after-loading-admob-firebase-ad
            nightModeOn();
        } else {
            nightModeOff();
        }
    }

    // calling nightModeOn() or nightModeOff() in onCreate() seems to cause the
    // activity to be recreated after onCreate() finishes
    // unclear if it's expected behavior or a bug
    private void nightModeOn() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
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

    // region listeners ----------------------------------------------------------------------------

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        darkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, true);

        if (darkMode) {
            new WebView(this); //DayNight fix: https://stackoverflow.com/questions/44035654/broken-colors-in-daynight-theme-after-loading-admob-firebase-ad
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

        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            Fragment loginFragment = fm.findFragmentByTag(TAG_WEBVIEW_LOGIN_FRAGMENT);

            if (loginFragment != null) {
                fm.beginTransaction().remove(loginFragment).commit();
            }

            Uri uri = intent.getData();
            String error = uri.getQueryParameter("error");
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");

            if (error != null && error.equals("access_denied")) {
                Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.fetchUserOAuthTokenSync(code);
            }
        }
    }

    // endregion listeners -------------------------------------------------------------------------
}
