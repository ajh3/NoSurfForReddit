package com.aaronhalbert.nosurfforreddit.fragments;

import android.net.Uri;
import android.view.View;

import com.aaronhalbert.nosurfforreddit.ExternalBrowser;

import androidx.navigation.Navigation;

/* for displaying a Reddit link-type post, as opposed to a self-type
 *
 * also see SelfPostFragment */

public class LinkPostFragment extends PostFragment {
    public static LinkPostFragment newInstance() {
        return new LinkPostFragment();
    }

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
            LinkPostFragmentDirections.GotoUrlAction action
                    = LinkPostFragmentDirections.gotoUrlAction(url);

            Navigation.findNavController(view).navigate(action);
        }
    }
}
