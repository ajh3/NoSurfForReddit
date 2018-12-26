package com.aaronhalbert.nosurfforreddit

import android.content.Context
import android.content.Intent
import android.net.Uri

class ExternalBrowser(private val context: Context) {

    fun launchExternalBrowser(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)

        // avoid crashing if there is no supported external browser installed on the device
        intent.resolveActivity(context.packageManager)?.let { context.startActivity(intent) }
    }
}
