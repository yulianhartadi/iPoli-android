package mypoli.android.common.view

import android.animation.Animator
import android.view.View
import android.view.ViewAnimationUtils

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/31/17.
 */
class RevealAnimator  {

    fun create(view: View, anchorView: View = view, reverse: Boolean = false): Animator {
        val finalRadius = Math.max(view.width / 2, view.height / 2).toFloat()
        return ViewAnimationUtils.createCircularReveal(view,
            anchorView.x.toInt() + anchorView.width / 2,
            anchorView.y.toInt() + anchorView.height / 2,
            if (reverse) finalRadius else 0f,
            if (reverse) 0f else finalRadius
        )
    }

    fun createWithStartRadius(view: View, startRadius: Float, reverse: Boolean = false): Animator {
        val radius = Math.max(view.width / 2, view.height / 2).toFloat()
        return ViewAnimationUtils.createCircularReveal(view,
            view.width / 2,
            view.height / 2,
            startRadius,
            if (reverse) 0f else radius
        )
    }


    fun createWithEndRadius(view: View, endRadius: Float, reverse: Boolean = false): Animator {
        val radius = Math.max(view.width / 2, view.height / 2).toFloat()
        return ViewAnimationUtils.createCircularReveal(view,
            view.width / 2,
            view.height / 2,
            if (reverse) radius else 0f,
            endRadius
        )
    }
}