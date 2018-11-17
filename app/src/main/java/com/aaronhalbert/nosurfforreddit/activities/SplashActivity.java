package com.aaronhalbert.nosurfforreddit.activities;

import android.content.Intent;
import android.os.Bundle;

import com.aaronhalbert.nosurfforreddit.viewmodel.SplashActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.SplashActivityViewModelFactory;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProviders;

/* all this activity does is display the app logo as a splash screen by overriding
 * android:windowBackground. It watches viewModel.getAllPostsViewStateLiveData(), which returns
 * data as soon as the app is initialized, regardless of the user's login status. Thus, when the
 * observer is called, we know that we're ready to launch MainActivity, whether or not the user is
 * logged in.
 *
 * The reason we use an observer to wait on data loading instead of directly calling startActivity()
 * is because we are not only trying to replace the default blank white loading screen with the
 * logo, but we also want to make sure there is no "gap" between the splash activity finishing and
 * initial network calls finishing, during which the RecyclerView would be shown blank, without any
 * data binded to it.
 *
 * This way, the RecyclerView is already populated with data immediately when the splash disappears,
 * which is prettier. */

public class SplashActivity extends BaseActivity {
    @SuppressWarnings("WeakerAccess") @Inject SplashActivityViewModelFactory splashActivityViewModelFactory;
    private SplashActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getPresentationComponent().inject(this);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders
                .of(this, splashActivityViewModelFactory)
                .get(SplashActivityViewModel.class);

        Intent i = new Intent(this, MainActivity.class);

        viewModel.getAllPostsViewStateLiveData().observe(this, postsViewState -> {
            startActivity(i);
            finish();
        });
    }
}
