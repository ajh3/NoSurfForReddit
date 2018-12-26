package com.aaronhalbert.nosurfforreddit

import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class DayNightHelper(private val fragmentActivity: FragmentActivity) {

    fun nightModeOn(amoledNightMode: Boolean) {
        WebView(fragmentActivity) //DayNight fix: https://stackoverflow.com/questions/44035654/broken-colors-in-daynight-theme-after-loading-admob-firebase-ad
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (amoledNightMode) amoledNightModeOn()
    }

    private fun amoledNightModeOn() {
        fragmentActivity
                .window
                .decorView
                .setBackgroundColor(ContextCompat.getColor(fragmentActivity, R.color.colorAmoledNightBg))
    }

    fun nightModeOff() {
        WebView(fragmentActivity) // see comment in nightModeOn()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
