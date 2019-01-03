package com.aaronhalbert.nosurfforreddit.di.presentation;

import com.aaronhalbert.nosurfforreddit.ui.detail.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.ui.detail.PostFragment;
import com.aaronhalbert.nosurfforreddit.ui.login.LoginFragment;
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivity;
import com.aaronhalbert.nosurfforreddit.ui.main.ViewPagerFragment;
import com.aaronhalbert.nosurfforreddit.ui.master.PostsFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {PresentationModule.class, ViewModelModule.class})
public interface PresentationComponent {
    void inject(MainActivity mainActivity);
    void inject(NoSurfWebViewFragment noSurfWebViewFragment);
    void inject(ViewPagerFragment viewPagerFragment);
    void inject(PostFragment postFragment);
    void inject(PostsFragment postsFragment);
    void inject(LoginFragment loginFragment);
}
