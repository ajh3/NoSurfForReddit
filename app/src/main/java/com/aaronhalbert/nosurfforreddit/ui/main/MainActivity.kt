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

package com.aaronhalbert.nosurfforreddit.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.aaronhalbert.nosurfforreddit.BaseActivity
import com.aaronhalbert.nosurfforreddit.R
import com.aaronhalbert.nosurfforreddit.data.network.auth.AuthenticatorUtils
import com.aaronhalbert.nosurfforreddit.data.settings.SettingsStore
import com.aaronhalbert.nosurfforreddit.utils.DayNightPicker
import com.aaronhalbert.nosurfforreddit.utils.SplashScreen
import com.aaronhalbert.nosurfforreddit.utils.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject

private const val LOGIN_FAILED_ERROR_MESSAGE = "Error: Login failed"
private const val NETWORK_ERROR_MESSAGE = "Network error!"
private const val NIGHT_MODE = "nightMode"
private const val AMOLED_NIGHT_MODE = "amoledNightMode"

class MainActivity : BaseActivity() {
    @Inject lateinit var settingsStore: SettingsStore
    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var authenticatorUtils: AuthenticatorUtils
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var navController: NavController
    private val dayNightPicker = DayNightPicker(this)
    private val sharedPrefsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (NIGHT_MODE == key || AMOLED_NIGHT_MODE == key) {
                pickDayNightMode()
                recreate()
            }
        }
    private var nightMode: Boolean = false
    private var amoledNightMode: Boolean = false

    // region lifecycle methods --------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        /* AppCompatDelegate.getDefaultNightMode() only works correctly when called
         * before super.onCreate(), otherwise, the activity recreates itself */
        pickDayNightMode()
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(application, R.xml.preferences, false)
        setContentView(R.layout.activity_main)

        /* only necessary to specify the factory the first time here, subsequent calls to
         * ViewModelProviders.of for MainActivityViewModel will get the right ViewModel without
         * specifying the factory */
        viewModel = ViewModelProviders
            .of(this, viewModelFactory)[MainActivityViewModel::class.java]

        /* only run the splash animation on fresh app launch */
        if (savedInstanceState == null) initSplash()
        initNavComponent()
        subscribeToNetworkErrors()
    }

    override fun onResume() {
        super.onResume()
        settingsStore.registerListener(sharedPrefsListener)
    }

    override fun onPause() {
        super.onPause()
        settingsStore.unregisterListener(sharedPrefsListener)
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region init methods -------------------------------------------------------------------------

    private fun initSplash() {
        SplashScreen(
            logo,
            this,
            viewModel.allPostsViewStateLiveData
        )
            .setupSplashAnimation()
    }

    private fun initNavComponent() {
        navController = NavHostFragment.findNavController(
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!
        )

        /* NavigationUI uses AppBarConfiguration to manage the "UP" button in top-left corner */
        val appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        setSupportActionBar(toolbar)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
    }

    private fun pickDayNightMode() {
        nightMode = settingsStore.isNightMode
        amoledNightMode = settingsStore.isAmoledNightMode

        if (nightMode) {
            dayNightPicker.nightModeOn(amoledNightMode)
        } else {
            dayNightPicker.nightModeOff()
        }
    }

    // endregion init methods ----------------------------------------------------------------------

    // region observers/listeners ------------------------------------------------------------------

    private fun subscribeToNetworkErrors() {
        viewModel.networkErrorsLiveData.observe(this,
            Observer {
                it.contentIfNotHandled?.let {
                    Toast.makeText(
                        this,
                        NETWORK_ERROR_MESSAGE,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    /* captures result from Reddit login page using custom redirect URI nosurfforreddit://oauth .
     * Intent filter is configured in manifest. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (Intent.ACTION_VIEW == intent.action) {
            val code = authenticatorUtils.extractCodeFromIntent(intent)

            if (code.isEmpty()) {
                Log.e(javaClass.toString(), LOGIN_FAILED_ERROR_MESSAGE)
                Toast.makeText(this, LOGIN_FAILED_ERROR_MESSAGE, Toast.LENGTH_LONG).show()
                navController.navigateUp()
            } else {
                viewModel.logUserIn(code)
                navController.navigateUp()
            }
        }
    }

    // endregion observers/listeners ---------------------------------------------------------------
}
