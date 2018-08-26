package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.fragments.HelloWorldFragment;
import com.aaronhalbert.nosurfforreddit.fragments.HomePostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

public class MainActivity extends AppCompatActivity implements ViewPagerFragment.OnFragmentInteractionListener, PostsAdapter.RecyclerViewOnClickCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NoSurfViewModel viewModel = ViewModelProviders.of(this).get(NoSurfViewModel.class);

        viewModel.initApp();

        getSupportFragmentManager().beginTransaction().add(R.id.main_activity_base_view, ViewPagerFragment.newInstance("abc", "def")).commit();

        /* Disable StrictMode due to Untagged socket detected errors
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
        */

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onItemClick(String url, boolean isSelf) {


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_base_view, HelloWorldFragment.newInstance("abc", "def"))
                .addToBackStack(null)
                .commit();

    }
}
