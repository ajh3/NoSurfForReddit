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
        setContentView(com.aaronhalbert.nosurfforreddit.R.layout.activity_main);

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

        /* This works as expected

        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_base_view, HelloWorldFragment.newInstance("abc", "def")).addToBackStack(null).commit();

        */

        //confirm the childFragmentManager being called below is ViewPagerFragment's childFragmentManager, the same one being passed into the FragmentPagerAdapter
        Log.e(getClass().toString(), getSupportFragmentManager().findFragmentById(R.id.main_activity_base_view).getChildFragmentManager().toString());

        //... but trying to replace the ViewPager results in a blank screen... no Hello World text in sight
        getSupportFragmentManager()
                .findFragmentById(R.id.main_activity_base_view)
                .getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.pager, HelloWorldFragment.newInstance("abc", "def"))
                .addToBackStack(null)
                .commit();
    }
}
