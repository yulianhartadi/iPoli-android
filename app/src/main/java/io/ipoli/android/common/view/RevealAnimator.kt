package io.ipoli.android.common.view

import android.animation.Animator
import android.view.View
import android.view.ViewAnimationUtils

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 10/31/17.
 */
class RevealAnimator(private val view: View, private val anchorView: View = view, private val reverse: Boolean = false)  {

    fun create(): Animator {
        val finalRadius = Math.sqrt((view.width * view.width + view.height * view.height).toDouble()).toFloat()
        return ViewAnimationUtils.createCircularReveal(view,
            anchorView.x.toInt() + anchorView.width / 2,
            anchorView.y.toInt() + anchorView.height / 2,
            if (reverse) finalRadius else 0f,
            if (reverse) 0f else finalRadius
        )
    }
}