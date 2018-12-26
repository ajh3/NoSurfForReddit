package com.aaronhalbert.nosurfforreddit

import android.animation.AnimatorInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState

class SplashHelper(
        private val iv: ImageView,
        private val fragmentActivity: FragmentActivity,
        private val allPostsViewStateLiveData: LiveData<PostsViewState>) {

    /* we implement the splash animation as a View inside MainActivity rather than having a
     * separate SplashActivity. The latter would be slower, and we don't want to display the
     * splash for any longer than is necessary. */
    fun setupSplashAnimation() {
        val refreshDrawableAnimator =
                AnimatorInflater.loadAnimator(fragmentActivity, R.animator.splash_animation)

        iv.setImageDrawable(
                ResourcesCompat
                        .getDrawable(fragmentActivity.resources, R.drawable.web_hi_res_512, null))

        refreshDrawableAnimator.setTarget(iv)
        refreshDrawableAnimator.start()

        setupSplashCanceler()
    }

    /* clear the splash screen as soon as data have arrived */
    private fun setupSplashCanceler() {
        allPostsViewStateLiveData.observe(fragmentActivity,
                Observer { iv.visibility = View.GONE })
    }
}
