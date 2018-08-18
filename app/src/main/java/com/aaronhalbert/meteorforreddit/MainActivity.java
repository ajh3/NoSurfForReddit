package com.aaronhalbert.meteorforreddit;

import android.arch.lifecycle.ViewModelProviders;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.aaronhalbert.meteorforreddit.R.layout.activity_main);

        RedditViewModel viewModel = ViewModelProviders.of(this).get(RedditViewModel.class);

        viewModel.initApp();

        ViewPager pager = findViewById(com.aaronhalbert.meteorforreddit.R.id.pager);
        TabLayout tabs = findViewById(com.aaronhalbert.meteorforreddit.R.id.tabs);

        pager.setAdapter(new RedditFragmentPagerAdapter(getSupportFragmentManager()));

        tabs.setupWithViewPager(pager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        /* Disable StrictMode due to Untagged socket detected errors
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
        */



    }


}
