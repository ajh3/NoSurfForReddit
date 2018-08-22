package com.aaronhalbert.nosurfforreddit;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aaronhalbert.nosurfforreddit.adapters.NoSurfFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.aaronhalbert.nosurfforreddit.R.layout.activity_main);

        NoSurfViewModel viewModel = ViewModelProviders.of(this).get(NoSurfViewModel.class);

        viewModel.initApp();

        ViewPager pager = findViewById(com.aaronhalbert.nosurfforreddit.R.id.pager);
        TabLayout tabs = findViewById(com.aaronhalbert.nosurfforreddit.R.id.tabs);

        pager.setAdapter(new NoSurfFragmentPagerAdapter(getSupportFragmentManager()));

        tabs.setupWithViewPager(pager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        /* Disable StrictMode due to Untagged socket detected errors
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
        */



    }


}
