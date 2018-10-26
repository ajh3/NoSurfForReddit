package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.activities.MainActivity;
import com.google.android.material.tabs.TabLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.adapters.NoSurfFragmentPagerAdapter;

public class ViewPagerFragment extends BaseFragment {
    private ViewPager pager;
    private NoSurfFragmentPagerAdapter noSurfFragmentPagerAdapter;
    private NoSurfViewModel viewModel;

    MenuItem loginMenuItem;
    MenuItem logoutMenuItem;

    public static ViewPagerFragment newInstance() {
        ViewPagerFragment fragment = new ViewPagerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(getActivity()).get(NoSurfViewModel.class);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_view_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pager = view.findViewById(R.id.view_pager_fragment_pager);
        TabLayout tabs = view.findViewById(R.id.view_pager_fragment_tabs);
        noSurfFragmentPagerAdapter = new NoSurfFragmentPagerAdapter(getChildFragmentManager());

        pager.setAdapter(noSurfFragmentPagerAdapter);
        tabs.setupWithViewPager(pager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view_pager_fragment, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //TODO: define an interface instead of calling getActivity()
        switch(item.getItemId()) {
            case R.id.refresh:
                if (pager.getCurrentItem() == 0) {
                    viewModel.refreshAllPosts();
                } else {
                    viewModel.refreshSubscribedPosts();
                }
                return true;
            case R.id.login:
                ((MainActivity) getActivity()).login();
                return true;
            case R.id.logout:
                ((MainActivity) getActivity()).logout();
                return true;
            case R.id.settings:
                ((MainActivity) getActivity()).launchPreferences();
                return true;
            case R.id.about:
                ((MainActivity) getActivity()).launchAboutScreen();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        loginMenuItem = menu.findItem(R.id.login);
        logoutMenuItem = menu.findItem(R.id.logout);

        if (viewModel.isUserLoggedIn()) {
            loginMenuItem.setVisible(false);
            logoutMenuItem.setVisible(true);
        } else {
            loginMenuItem.setVisible(true);
            logoutMenuItem.setVisible(false);
        }
    }
}
