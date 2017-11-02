package io.ipoli.android.common.view

import android.animation.Animator
import android.view.View
import android.view.ViewAnimationUtils

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 10/31/17.
 */
class RevealAnimator  {

    fun create(view: View, anchorView: View = view, reverse: Boolean = false): Animator {
        val finalRadius = Math.max(view.width, view.height).toFloat()
        return ViewAnimationUtils.createCircularReveal(view,
            anchorView.x.toInt() + anchorView.width / 2,
            anchorView.y.toInt() + anchorView.height / 2,
            if (reverse) finalRadius else 0f,
            if (reverse) 0f else finalRadius
        )
    }

    fun createWithStartRadius(view: View, startRadius: Float, anchorView: View = view, reverse: Boolean = false): Animator {
        val finalRadius = Math.max(view.width, view.height).toFloat()
        return ViewAnimationUtils.createCircularReveal(view,
            anchorView.x.toInt() + anchorView.width / 2,
            anchorView.y.toInt() + anchorView.height / 2,
            if (reverse) finalRadius else 0f,
            if (reverse) 0f else finalRadius
        )
    }


    fun createWithEndRadius(view: View, endRadius: Float, anchorView: View = view, reverse: Boolean = false): Animator {
        val finalRadius = Math.max(view.width, view.height).toFloat()
        return ViewAnimationUtils.createCircularReveal(view,
            anchorView.x.toInt() + anchorView.width / 2,
            anchorView.y.toInt() + anchorView.height / 2,
            if (reverse) finalRadius else 0f,
            if (reverse) 0f else finalRadius
        )
    }

//    fun create(view: View, startRadius: Float, endRadius: Float, anchorView: View = view, reverse: Boolean = false): Animator {
//        val finalRadius = Math.max(view.width, view.height).toFloat()
//        return ViewAnimationUtils.createCircularReveal(view,
//            anchorView.x.toInt() + anchorView.width / 2,
//            anchorView.y.toInt() + anchorView.height / 2,
//            if (reverse) finalRadius else 0f,
//            if (reverse) 0f else finalRadius
//        )
//    }
}