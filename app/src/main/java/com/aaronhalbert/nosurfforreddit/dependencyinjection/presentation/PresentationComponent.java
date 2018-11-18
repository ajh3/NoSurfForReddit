package com.aaronhalbert.nosurfforreddit.dependencyinjection.presentation;

import com.aaronhalbert.nosurfforreddit.activities.MainActivity;
import com.aaronhalbert.nosurfforreddit.activities.SplashActivity;
import com.aaronhalbert.nosurfforreddit.fragments.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.fragments.PostFragment;
import com.aaronhalbert.nosurfforreddit.fragments.PostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {PresentationModule.class, ViewModelModule.class})
public interface PresentationComponent {
    void inject(MainActivity mainActivity);
    void inject(SplashActivity splashActivity);
    void inject(NoSurfWebViewFragment noSurfWebViewFragment);
    void inject(ViewPagerFragment viewPagerFragment);
    void inject(PostFragment postFragment);
    void inject(PostsFragment postsFragment);
}
