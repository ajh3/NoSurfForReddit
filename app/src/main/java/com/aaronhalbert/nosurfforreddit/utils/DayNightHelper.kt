/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.utils

import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aaronhalbert.nosurfforreddit.R

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
