package com.aaronhalbert.nonaddictivereddit4droid;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.karim.MaterialTabs;

public class MainActivity extends AppCompatActivity {
    private ViewPager pager;
    private RedditFragmentPagerAdapter adapter;
    private MaterialTabs tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);

        pager.setAdapter(new RedditFragmentPagerAdapter(getSupportFragmentManager()));
        tabs.setViewPager(pager);

    }
}
