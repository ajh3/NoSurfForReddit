package com.aaronhalbert.nosurfforreddit;

import android.content.Intent;
import android.view.Menu;

import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;

import static com.aaronhalbert.nosurfforreddit.BuildConfig.REDDIT_URL_BASE;

public class ShareHelper {
    private static final String SHARE_POST = "Share post...";

    private ShareActionProvider shareActionProvider;

    public void setupShareActionProvider(Menu menu) {
        shareActionProvider = (ShareActionProvider) MenuItemCompat
                .getActionProvider(menu.findItem(R.id.menu_item_share));
    }

    public Intent createShareIntent(String lastClickedPostPermalink) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, SHARE_POST);
        i.putExtra(Intent.EXTRA_TEXT, REDDIT_URL_BASE + lastClickedPostPermalink);

        return i;
    }

    public void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }
}
