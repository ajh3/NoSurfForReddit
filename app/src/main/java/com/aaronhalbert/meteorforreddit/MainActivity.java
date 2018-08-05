package com.aaronhalbert.meteorforreddit;

import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.karim.MaterialTabs;

public class MainActivity extends AppCompatActivity {
    private RedditFragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.aaronhalbert.meteorforreddit.R.layout.activity_main);

        ViewPager pager = findViewById(com.aaronhalbert.meteorforreddit.R.id.pager);
        MaterialTabs tabs = findViewById(com.aaronhalbert.meteorforreddit.R.id.tabs);

        pager.setAdapter(new RedditFragmentPagerAdapter(getSupportFragmentManager()));
        tabs.setViewPager(pager);

        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }

    }


}
