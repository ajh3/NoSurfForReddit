package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.fragments.LinkPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfPreferenceFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.SelfPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LinkPostFragment.OnFragmentInteractionListener,
        PostsAdapter.RecyclerViewOnClickCallback {

    private static final String KEY_USER_ACCESS_TOKEN = "userAccessToken";
    private static final String KEY_USER_ACCESS_REFRESH_TOKEN = "userAccessRefreshToken";

    NoSurfViewModel viewModel = null;

    ViewPagerFragment viewPagerFragment;

    SharedPreferences preferences;

    MenuItem loginMenuItem;
    MenuItem logoutMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(getPackageName() + "oauth", MODE_PRIVATE);

        viewModel = ViewModelProviders.of(this).get(NoSurfViewModel.class);

        viewModel.initApp();

        if (savedInstanceState == null) {

            viewPagerFragment = ViewPagerFragment.newInstance("abc", "def");

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_activity_frame_layout, viewPagerFragment)
                    .commit();

        }

        /* Disable StrictMode due to Untagged socket detected errors
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
        */
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e(getClass().toString(), "zzzz the adapter is: " + viewPagerFragment.getNoSurfFragmentPagerAdapter().toString());

        Intent intent = getIntent();

        if (intent.getAction().equals(Intent.ACTION_VIEW)) {

            Uri uri = intent.getData();
            String error = uri.getQueryParameter("error");
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");

            if (error != null && error.equals("access_denied")) {
                Log.e(getClass().toString(), "access denied");

            } else {
                viewModel.requestUserOAuthToken(code);
                String asdf = preferences.getString(KEY_USER_ACCESS_REFRESH_TOKEN, null);
                Log.e(getClass().toString(), "refresh key is " + asdf);

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                asdf = preferences.getString(KEY_USER_ACCESS_REFRESH_TOKEN, null);
                Log.e(getClass().toString(), "refresh key is " + asdf);


                //viewPagerFragment.getNoSurfFragmentPagerAdapter().notifyDataSetChanged();   //what happens if this called before token is stored?
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        loginMenuItem = menu.findItem(R.id.login);
        logoutMenuItem = menu.findItem(R.id.logout);



        return super.onCreateOptionsMenu(menu);
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
                //onRefresh();
                return true;
            case R.id.settings:
                //launchPreferences();
                return true;
            case R.id.about:
                //about();
                return true;
        }
        return (super.onOptionsItemSelected(item)); //what does this do?
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (viewModel.isUserLoggedIn()) {
            setMenuLoggedIn();
        } else {
            setMenuLoggedOut();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void setMenuLoggedIn() {
        loginMenuItem.setVisible(false);
        logoutMenuItem.setVisible(true);
    }

    public void setMenuLoggedOut() {
        loginMenuItem.setVisible(true);
        logoutMenuItem.setVisible(false);
    }

    public void launchWebView(String url) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, NoSurfWebViewFragment.newInstance(url))
                .addToBackStack(null)
                .commit();
    }


    public void launchSelfPost(String title, String selfText, String id, String subreddit, String author, int score) {
        viewModel.requestPostCommentsListing(id);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, SelfPostFragment.newInstance(title, selfText, id, subreddit, author, score))
                .addToBackStack(null)
                .commit();
    }

    public void launchLinkPost(String title, String imageUrl, String url, String gifUrl, String id, String subreddit, String author, int score) {
        viewModel.requestPostCommentsListing(id);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, LinkPostFragment.newInstance(title, imageUrl, url, gifUrl, id, subreddit, author, score))
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


            launchWebView(loginUrl);

    }

    public void logout() {

        preferences
                .edit()
                .putString(KEY_USER_ACCESS_TOKEN, null)
                .putString(KEY_USER_ACCESS_REFRESH_TOKEN, null)
                .commit();

        viewPagerFragment.getNoSurfFragmentPagerAdapter().notifyDataSetChanged();
    }

    public void launchPreferences() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, NoSurfPreferenceFragment.newInstance("a", "b"))
                .addToBackStack(null)
                .commit();
    }

    private String generateRandomAlphaNumericString() {
        return UUID.randomUUID().toString();
    }



}
