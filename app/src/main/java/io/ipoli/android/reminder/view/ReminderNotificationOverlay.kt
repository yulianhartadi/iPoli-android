package io.ipoli.android.reminder.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateInterpolator
import io.ipoli.android.R
import io.ipoli.android.common.view.BaseOverlayViewController
import kotlinx.android.synthetic.main.view_reminder.view.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/13/17.
 */

class ReminderNotificationOverlay(private val listener: OnClickListener) {

    interface OnClickListener {
        fun onDismiss()
        fun onSnooze()
        fun onDone()
    }

    fun show(context: Context) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.view_reminder, null) as ViewGroup

        view.dismiss.setOnClickListener {
            listener.onDismiss()
        }

        view.snooze.setOnClickListener {
            listener.onSnooze()
        }

        view.done.setOnClickListener {
            listener.onDone()
        }

        val focusable = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED.
            or(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN).
            or(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL).
            or(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            BaseOverlayViewController.WindowOverlayCompat.TYPE_SYSTEM_ERROR,
            focusable,
            PixelFormat.TRANSLUCENT)

        windowManager.addView(view, layoutParams)

        view.post {
                        startShowAnimation(view, context)
//            startHideAnimation(view, context)
        }
    }

    private fun startShowAnimation(view: ViewGroup, context: Context) {

        val backgroundView = view.view

        val bounds = Rect()
        backgroundView.getDrawingRect(bounds)

        val location = IntArray(2)
        backgroundView.getLocationOnScreen(location)

        val startRadius = (getScreenHeight(context) - location[1]).toFloat()
        val centerX = bounds.centerX()
        val centerY = startRadius.toInt() / 2

        val anim = ViewAnimationUtils.createCircularReveal(
            backgroundView,
            centerX, centerY,
            0f, startRadius
        )
        anim.interpolator = AnticipateInterpolator(2.0f)
        anim.duration = context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
//        anim.duration = 2000
        anim.start()

        val petAnimator = createPetAnimator(view.pet, context)
        val petStateAnimator = createPetAnimator(view.petState, context)
        petAnimator.start()
        petStateAnimator.start()
    }

    private fun getScreenHeight(context: Context): Int {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    private fun startHideAnimation(view: ViewGroup, context: Context) {
        val backgroundView = view.view

        val bounds = Rect()
        backgroundView.getDrawingRect(bounds)

        val location = IntArray(2)
        backgroundView.getLocationOnScreen(location)

        val startRadius = (getScreenHeight(context) - location[1]).toFloat()
        val centerX = bounds.centerX()
        val centerY = startRadius.toInt() / 2

        val anim = ViewAnimationUtils.createCircularReveal(
            backgroundView,
            centerX, centerY,
            startRadius, 0f
        )
        anim.interpolator = AnticipateInterpolator(2.0f)

        anim.duration = context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                backgroundView.visibility = View.INVISIBLE
            }
        })

        anim.start()
    }

    private fun createShowBackgroundAnimator(view: View, context: Context): AnimatorSet =
        createBackgroundAnimator(view, context, 0f, 1f)

    private fun createHideBackgroundAnimator(view: View, context: Context): AnimatorSet =
        createBackgroundAnimator(view, context, 1f, 0f)

    private fun createBackgroundAnimator(view: View, context: Context, fromValue: Float, toValue: Float): AnimatorSet {
        val duration = context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()

        val fadeAnimator = ObjectAnimator.ofFloat(view, "alpha", fromValue, toValue)
        fadeAnimator.duration = duration
        fadeAnimator.interpolator = AccelerateDecelerateInterpolator()

        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", fromValue, toValue)
        scaleXAnimator.duration = duration
        scaleXAnimator.interpolator = AccelerateDecelerateInterpolator()

        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", fromValue, toValue)
        scaleYAnimator.duration = duration
        scaleYAnimator.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeAnimator, scaleXAnimator, scaleYAnimator)
        return animatorSet
    }

    private fun createPetAnimator(view: View, context: Context): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(view, "y", view.y + view.height, view.y)
        animator.duration = context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.startDelay = 300
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                view.visibility = View.VISIBLE
            }
        })

        return animator
    }
}