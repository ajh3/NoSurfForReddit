package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.fragments.GifvFragment;
import com.aaronhalbert.nosurfforreddit.fragments.HomePostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ImageFragment;
import com.aaronhalbert.nosurfforreddit.fragments.LinkPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfPreferenceFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.SelfPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LinkPostFragment.OnFragmentInteractionListener, PostsAdapter.RecyclerViewOnClickCallback, HomePostsFragment.HomePostsLoginCallback {

    private static final String TAG_VIEW_PAGER = "viewPager";

    NoSurfViewModel viewModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(NoSurfViewModel.class);

        viewModel.initApp();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_activity_frame_layout, ViewPagerFragment.newInstance("abc", "def"), TAG_VIEW_PAGER)
                    .commit();
        }

        Intent intent = getIntent();
        if ((intent.getAction()).equals(Intent.ACTION_VIEW)) {
            Uri uri = intent.getData();
            String error = uri.getQueryParameter("error");
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");

            Log.e(getClass().toString(), "error: " + error + "code: " + code + "state: " + state);

            viewModel.requestUserOAuthToken(code);

        } else if (!viewModel.isUserLoggedIn()){
            launchLoginScreen();
        }

        /* Disable StrictMode due to Untagged socket detected errors
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);

        return (super.onCreateOptionsMenu(menu));   //is calling super needed?
        //return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                return true;

            case R.id.settings:
                launchPreferences();
                return true;

            case R.id.about:
                return true;
        }
        return (super.onOptionsItemSelected(item)); //what does this do?
    }


    public void launchWebView(String url) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, NoSurfWebViewFragment.newInstance(url))
                .addToBackStack(null)
                .commit();
    }


    public void launchSelfPost(String title, String selfText, String id) {
        viewModel.requestPostCommentsListing(id);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, SelfPostFragment.newInstance(title, selfText, id))
                .addToBackStack(null)
                .commit();
    }

    public void launchLinkPost(String title, String imageUrl, String url, String gifUrl, String id) {
        viewModel.requestPostCommentsListing(id);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, LinkPostFragment.newInstance(title, imageUrl, url, gifUrl, id))
                .addToBackStack(null)
                .commit();
    }

    public void launchLoginScreen() {
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
