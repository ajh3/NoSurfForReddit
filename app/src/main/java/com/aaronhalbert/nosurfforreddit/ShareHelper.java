package com.aaronhalbert.nosurfforreddit;

import android.content.Context;
import android.content.Intent;

import static com.aaronhalbert.nosurfforreddit.BuildConfig.REDDIT_URL_BASE;

/* I prefer this custom solution to ShareActionProvider. Don't like the "frequent" shortcut
 * it puts in the action bar, or the border it draws between the icons. The standard chooser
 * is preferable IMO. */
public class ShareHelper {
    private static final String SHARE_POST = "Share post...";

    private Context context;
    private Intent intent;

    public ShareHelper(Context context) {
        this.context = context;
    }

    public void createShareIntent(String lastClickedPostPermalink) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, SHARE_POST);
        i.putExtra(Intent.EXTRA_TEXT, REDDIT_URL_BASE + lastClickedPostPermalink);

        this.intent = i;
    }

    public void launchShareIntent() {
        context.startActivity(Intent.createChooser(intent, context.getResources().getText(R.string.share)));
    }
}
