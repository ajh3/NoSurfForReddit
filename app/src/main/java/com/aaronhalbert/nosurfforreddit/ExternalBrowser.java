package com.aaronhalbert.nosurfforreddit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ExternalBrowser {
    private final Context context;

    public ExternalBrowser(Context context) {
        this.context = context;
    }

    public void launchExternalBrowser(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
}
