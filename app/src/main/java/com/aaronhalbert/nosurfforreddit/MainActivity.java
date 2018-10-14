package com.aaronhalbert.nosurfforreddit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.fragments.AboutFragment;
import com.aaronhalbert.nosurfforreddit.fragments.PostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.LoginFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfPreferenceFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        PostFragment.OnFragmentInteractionListener,
        PostsAdapter.launchPostCallback,
        LoginFragment.OnLoginFragmentButtonListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG_WEBVIEW_LOGIN = "webviewLoginTag";
    private static final String TAG_VIEW_PAGER_FRAGMENT = "viewPagerFragmentTag";
    private static final String KEY_DARK_MODE = "darkMode";

    NoSurfViewModel viewModel;
    ViewPagerFragment viewPagerFragment;
    SharedPreferences preferences;

    boolean darkMode;

    MenuItem loginMenuItem;
    MenuItem logoutMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences.registerOnSharedPreferenceChangeListener(this);
        darkMode = preferences.getBoolean(KEY_DARK_MODE, false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(NoSurfViewModel.class);

        if (savedInstanceState == null) {
            viewModel.initApp();

            viewPagerFragment = ViewPagerFragment.newInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_activity_frame_layout, viewPagerFragment, TAG_VIEW_PAGER_FRAGMENT)
                    .commit();
        }

        /* Disable StrictMode due to Untagged socket detected errors
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
        */
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            
            FragmentManager fm = getSupportFragmentManager();
            Fragment loginFragment = fm.findFragmentByTag(TAG_WEBVIEW_LOGIN);

            if (loginFragment != null) {
                fm.beginTransaction().remove(loginFragment).commit();
            }

            Uri uri = intent.getData();
            String error = uri.getQueryParameter("error");
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");

            if (error != null && error.equals("access_denied")) {
                Log.e(getClass().toString(), "access denied");
            } else {
                viewModel.requestUserOAuthToken(code);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu); //what does this do?
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                login();
                return true;
            case R.id.logout:
                logout();
                return true;
            case R.id.refresh:
                viewModel.requestAllSubredditsListing();
                viewModel.requestHomeSubredditsListing();
                viewModel.requestPostCommentsListing("previous");
                return true;
            case R.id.settings:
                launchPreferences();
                return true;
            case R.id.about:
                launchAboutScreen();
                return true;
        }
        return (super.onOptionsItemSelected(item)); //what does this do?
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        loginMenuItem = menu.findItem(R.id.login);
        logoutMenuItem = menu.findItem(R.id.logout);

        if (viewModel.isUserLoggedIn()) {
            loginMenuItem.setVisible(false);
            logoutMenuItem.setVisible(true);
        } else {
            loginMenuItem.setVisible(true);
            logoutMenuItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu); //what does this do?
    }

    public void launchWebView(String url, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_activity_frame_layout, NoSurfWebViewFragment.newInstance(url), tag)
                .addToBackStack(null)
                .commit();
    }

    public void launchPost(int position, boolean isSelfPost, boolean isSubscribedPost) {

        String id;

        if (isSubscribedPost) {
            id = viewModel.getHomePostsLiveDataViewState().getValue().postData.get(position).id;
        } else {
            id = viewModel.getAllPostsLiveDataViewState().getValue().postData.get(position).id;
        }

        Log.e(getClass().toString(), "writing id to database");
        viewModel.insertReadPostId(id);
        viewModel.requestPostCommentsListing(id);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, PostFragment.newInstance(position, isSelfPost, isSubscribedPost))
                .addToBackStack(null)
                .commit();
    }

    public void login() {
        final String CLIENT_ID = "jPF59UF5MbMkWg";
        final String RESPONSE_TYPE = "code";
        final String STATE = generateRandomAlphaNumericString();
        final String REDIRECT_URI = "nosurfforreddit://oauth";
        final String DURATION = "permanent";
        final String SCOPE = "identity mysubreddits read";

        final String loginUrl = "https://www.reddit.com/api/v1/authorize.compact?client_id="
                + CLIENT_ID
                + "&response_type="
                + RESPONSE_TYPE
                + "&state="
                + STATE
                + "&redirect_uri="
                + REDIRECT_URI
                + "&duration="
                + DURATION
                + "&scope="
                + SCOPE;

        launchWebView(loginUrl, TAG_WEBVIEW_LOGIN);
    }

    public void logout() {
        viewModel.logout();
    }

    public void launchPreferences() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, NoSurfPreferenceFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    public void launchAboutScreen() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, AboutFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    private String generateRandomAlphaNumericString() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        darkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        }
    }
}
