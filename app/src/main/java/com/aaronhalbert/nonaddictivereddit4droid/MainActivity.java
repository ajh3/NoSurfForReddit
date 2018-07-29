package com.aaronhalbert.nonaddictivereddit4droid;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.karim.MaterialTabs;

public class MainActivity extends AppCompatActivity {
    private RedditFragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager pager = findViewById(R.id.pager);
        MaterialTabs tabs = findViewById(R.id.tabs);

        pager.setAdapter(new RedditFragmentPagerAdapter(getSupportFragmentManager()));
        tabs.setViewPager(pager);

    }
}
