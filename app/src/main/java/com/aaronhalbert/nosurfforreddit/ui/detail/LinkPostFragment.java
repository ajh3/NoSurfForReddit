package com.aaronhalbert.nosurfforreddit.ui.detail;

import android.net.Uri;
import android.view.View;

import com.aaronhalbert.nosurfforreddit.utils.ExternalBrowser;

import androidx.navigation.Navigation;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoUrlGlobalAction;

/* for displaying a Reddit link-type post, as opposed to a self-type
 *
 * also see SelfPostFragment */

public class LinkPostFragment extends PostFragment {

    @Override
    void setupPostViews() {
        fragmentPostBinding
                .postFragmentImageForLinkPostsOnly
                .setVisibility(View.VISIBLE);
    }

    @Override
    void launchLink(View view, String url) {
        if (externalBrowser) {
            ExternalBrowser e = new ExternalBrowser(getContext());
            e.launchExternalBrowser(Uri.parse(url));
        } else {
            GotoUrlGlobalAction action
                    = gotoUrlGlobalAction(url);

            Navigation.findNavController(view).navigate(action);
        }
    }
}
