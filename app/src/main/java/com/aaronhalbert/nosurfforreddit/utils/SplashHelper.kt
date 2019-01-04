package com.aaronhalbert.nosurfforreddit.utils

import android.animation.AnimatorInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.aaronhalbert.nosurfforreddit.R
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivity
import com.aaronhalbert.nosurfforreddit.ui.viewstate.PostsViewState

class SplashHelper(
    private val iv: ImageView,
    private val mainActivity: MainActivity,
    private val allPostsViewStateLiveData: LiveData<PostsViewState>
) {

    /* we implement the splash animation as a View inside MainActivity rather than having a
     * separate SplashActivity. The latter would be slower, and we don't want to display the
     * splash for any longer than is necessary. */
    fun setupSplashAnimation() {
        val refreshDrawableAnimator =
            AnimatorInflater.loadAnimator(mainActivity, R.animator.splash_animation)

        iv.setImageDrawable(
            ResourcesCompat
                .getDrawable(mainActivity.resources, R.drawable.web_hi_res_512, null)
        )

        refreshDrawableAnimator.setTarget(iv)
        refreshDrawableAnimator.start()

        setupSplashCanceler()
    }

    /* clear the splash screen as soon as data have arrived */
    private fun setupSplashCanceler() {
        allPostsViewStateLiveData.observe(mainActivity,
            Observer { iv.visibility = View.GONE })
    }
}
