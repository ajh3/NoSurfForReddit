package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aaronhalbert.nosurfforreddit.adapters.PostsAdapter;
import com.aaronhalbert.nosurfforreddit.fragments.ExternalPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.SelfPostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

public class MainActivity extends AppCompatActivity implements ViewPagerFragment.OnFragmentInteractionListener, PostsAdapter.RecyclerViewOnClickCallback {

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


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onItemClick(String url, boolean isSelf) {

        if (isSelf) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_activity_frame_layout, SelfPostFragment.newInstance("a", "b"))
                    .addToBackStack(null)
                    .commit();

        } else {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_activity_frame_layout, ExternalPostFragment.newInstance(url))
                    .addToBackStack(null)
                    .commit();
        }



    }
}
