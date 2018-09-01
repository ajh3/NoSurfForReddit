package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.fragments.GifvFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ImageFragment;
import com.aaronhalbert.nosurfforreddit.fragments.LinkPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.SelfPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

public class MainActivity extends AppCompatActivity implements LinkPostFragment.OnFragmentInteractionListener, PostsAdapter.RecyclerViewOnClickCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NoSurfViewModel viewModel = ViewModelProviders.of(this).get(NoSurfViewModel.class);

        viewModel.initApp();

        getSupportFragmentManager().beginTransaction().add(R.id.main_activity_frame_layout, ViewPagerFragment.newInstance("abc", "def")).commit();

        /* Disable StrictMode due to Untagged socket detected errors
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
        */

    }


    public void launchWebView(String url) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, NoSurfWebViewFragment.newInstance(url))
                .addToBackStack(null)
                .commit();
    }


    public void launchSelfPost(String title, String selfText) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, SelfPostFragment.newInstance(title, selfText))
                .addToBackStack(null)
                .commit();
    }

    public void launchLinkPost(String title, String imageUrl, String url, String gifUrl) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame_layout, LinkPostFragment.newInstance(title, imageUrl, url, gifUrl))
                .addToBackStack(null)
                .commit();
    }




}
