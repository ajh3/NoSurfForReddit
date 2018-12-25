package com.aaronhalbert.nosurfforreddit.activities

import android.animation.AnimatorInflater
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.aaronhalbert.nosurfforreddit.R
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfAccessDeniedLoginException
import com.aaronhalbert.nosurfforreddit.exceptions.NoSurfLoginException
import com.aaronhalbert.nosurfforreddit.repository.NoSurfAuthenticator.extractCodeFromIntent
import com.aaronhalbert.nosurfforreddit.repository.SettingsStore
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel
import com.aaronhalbert.nosurfforreddit.viewmodel.ViewModelFactory
import javax.inject.Inject

const val ACCESS_DENIED_ERROR_MESSAGE = "Error: Access denied"
const val LOGIN_FAILED_ERROR_MESSAGE = "Error: Login failed"
const val NETWORK_ERROR_MESSAGE = "Network error!"
const val NIGHT_MODE = "nightMode"
const val AMOLED_NIGHT_MODE = "amoledNightMode"

class MainActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var settingsStore: SettingsStore
    @Inject lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var navController: NavController

    private var nightMode: Boolean = false
    private var amoledNightMode: Boolean = false

    // region lifecycle methods --------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        presentationComponent.inject(this)
        initPrefs()
        initNightMode() // call before super to avoid MainActivity recreating itself
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* only necessary to specify the factory the first time here, subsequent calls to
         * ViewModelProviders.of for MainActivityViewModel will get the right ViewModel without
         * specifying the factory */
        viewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainActivityViewModel::class.java)

        /* only run the splash animation on fresh app launch */
        if (savedInstanceState == null) setupSplashAnimation()

        initNavComponent()
        subscribeToNetworkErrors()
    }

    override fun onResume() {
        super.onResume()

        settingsStore.registerListener(this)
    }

    override fun onPause() {
        super.onPause()

        settingsStore.unregisterListener(this)
    }

    // endregion lifecycle methods -----------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    private fun initPrefs() {
        PreferenceManager.setDefaultValues(application, R.xml.preferences, false)

        nightMode = settingsStore.isNightMode
        amoledNightMode = settingsStore.isAmoledNightMode
    }

    private fun initNightMode() {
        if (nightMode) {
            nightModeOn()
        } else {
            nightModeOff()
        }
    }

    private fun nightModeOn() {
        WebView(this) //DayNight fix: https://stackoverflow.com/questions/44035654/broken-colors-in-daynight-theme-after-loading-admob-firebase-ad
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (amoledNightMode) amoledNightModeOn()
    }

    private fun amoledNightModeOn() {
        window
                .decorView
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorAmoledNightBg))
    }

    private fun nightModeOff() {
        WebView(this) // see comment in nightModeOn()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun navUp() {
        navController.navigateUp()
    }

    private fun initNavComponent() {
        navController = Navigation.findNavController(findViewById(R.id.nav_host_fragment))

        /* NavigationUI uses AppBarConfiguration to manage the "UP" button in top-left corner */
        val appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
    }

    /* we implement the splash animation as a View inside MainActivity rather than having a
     * separate SplashActivity. The latter would be slower, and we don't want to display the
     * splash for any longer than is necessary. */
    private fun setupSplashAnimation() {
        val refreshDrawableAnimator = AnimatorInflater.loadAnimator(this, R.animator.splash_animation)
        val iv = findViewById<ImageView>(R.id.logo)
        iv.setImageDrawable(resources.getDrawable(R.drawable.web_hi_res_512, null))
        refreshDrawableAnimator.setTarget(iv)
        refreshDrawableAnimator.start()
        setupSplashCanceler()
    }

    /* clear the splash screen as soon as data have arrived */
    private fun setupSplashCanceler() {
        viewModel.allPostsViewStateLiveData.observe(this,
                Observer { findViewById<View>(R.id.logo).visibility = View.GONE })
    }

    // endregion helper methods --------------------------------------------------------------------

    // region observers/listeners ------------------------------------------------------------------

    private fun subscribeToNetworkErrors() {
        viewModel.networkErrorsLiveData.observe(this,
                Observer {
                    val n = it.contentIfNotHandled

                    n?.let { Toast.makeText(
                            this,
                            NETWORK_ERROR_MESSAGE,
                            Toast.LENGTH_LONG).show() }
                })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (NIGHT_MODE == key || AMOLED_NIGHT_MODE == key) {
            nightMode = settingsStore.isNightMode

            if (nightMode) {
                nightModeOn()
                recreate()
            } else {
                nightModeOff()
                recreate()
            }
        }
    }

    /* captures result from Reddit login page using custom redirect URI nosurfforreddit://oauth .
     * Intent filter is configured in manifest. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (Intent.ACTION_VIEW == intent.action) {
            val code: String

            try {
                code = extractCodeFromIntent(intent)
            } catch (e: NoSurfAccessDeniedLoginException) {
                Log.e(javaClass.toString(), ACCESS_DENIED_ERROR_MESSAGE, e)
                Toast.makeText(this, ACCESS_DENIED_ERROR_MESSAGE, Toast.LENGTH_LONG).show()
                navUp()
                return
            } catch (e: NoSurfLoginException) {
                Log.e(javaClass.toString(), LOGIN_FAILED_ERROR_MESSAGE, e)
                Toast.makeText(this, LOGIN_FAILED_ERROR_MESSAGE, Toast.LENGTH_LONG).show()
                navUp()
                return
            }

            viewModel.logUserIn(code)
            navUp()
        }
    }

    // endregion observers/listeners ---------------------------------------------------------------
}
