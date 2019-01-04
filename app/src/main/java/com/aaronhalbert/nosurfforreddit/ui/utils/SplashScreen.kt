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

package com.aaronhalbert.nosurfforreddit.ui.utils

import android.animation.AnimatorInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.aaronhalbert.nosurfforreddit.R
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivity
import com.aaronhalbert.nosurfforreddit.ui.viewstate.PostsViewState

class SplashScreen(
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
