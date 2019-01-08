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

package com.aaronhalbert.nosurfforreddit

import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import com.aaronhalbert.nosurfforreddit.di.application.ApplicationComponent
import com.aaronhalbert.nosurfforreddit.di.presentation.PresentationComponent
import com.aaronhalbert.nosurfforreddit.di.presentation.PresentationModule
import com.aaronhalbert.nosurfforreddit.di.presentation.ViewModelModule

fun noSurfLog(message: () -> String) {
    if (BuildConfig.DEBUG) Log.e("NoSurf", message())
}

fun Context.makeToast(text: String) = Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()

private const val INJECTION_ALREADY_PERFORMED = "Injection already performed on this activity"

abstract class BaseActivity : AppCompatActivity() {
    private var isInjectorUsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStrictMode()
    }

    /* use Java-style names for this Dagger code since it's called from classes that haven't
     * yet been converted to Kotlin */

    @UiThread
    fun getPresentationComponent(): PresentationComponent {
        if (isInjectorUsed) {
            throw RuntimeException(INJECTION_ALREADY_PERFORMED)
        }

        isInjectorUsed = true

        return getApplicationComponent()
            .newPresentationComponent(PresentationModule(this), ViewModelModule())
    }

    private fun getApplicationComponent(): ApplicationComponent {
        return (application as NoSurfApplication).applicationComponent
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            val builder = StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen()

            StrictMode.setThreadPolicy(builder.build())
        }
    }
}
