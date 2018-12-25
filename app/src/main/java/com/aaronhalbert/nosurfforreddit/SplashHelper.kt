package com.aaronhalbert.nosurfforreddit

import android.animation.AnimatorInflater
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState

class SplashHelper {

    /* we implement the splash animation as a View inside MainActivity rather than having a
     * separate SplashActivity. The latter would be slower, and we don't want to display the
     * splash for any longer than is necessary. */
    fun setupSplashAnimation(iv: ImageView, context: Context) {
        val refreshDrawableAnimator =
                AnimatorInflater.loadAnimator(context, R.animator.splash_animation)

        iv.setImageDrawable(
                ResourcesCompat
                        .getDrawable(context.resources, R.drawable.web_hi_res_512, null))

        refreshDrawableAnimator.setTarget(iv)
        refreshDrawableAnimator.start()
    }

    /* clear the splash screen as soon as data have arrived */
    fun setupSplashCanceler(
            iv: ImageView,
            allPostsViewStateLiveData: LiveData<PostsViewState>,
            fragmentActivity: FragmentActivity
    ) {
        allPostsViewStateLiveData.observe(fragmentActivity,
                Observer { iv.visibility = View.GONE })
    }
}
