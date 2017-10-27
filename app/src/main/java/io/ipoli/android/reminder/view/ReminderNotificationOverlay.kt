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
import android.view.ViewAnimationUtils


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

    private lateinit var overlayView: ViewGroup
    private lateinit var windowManager: WindowManager

    fun show(context: Context) {
        val inflater = LayoutInflater.from(context)
        overlayView = inflater.inflate(R.layout.view_reminder, null) as ViewGroup
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        initButtons()
        addViewToWindowManager(overlayView)

        show()
    }

    private fun show() {
        overlayView.post {
            startShowAnimation(overlayView)
        }
    }

    private fun initButtons() {
        overlayView.dismiss.setOnClickListener {
            listener.onDismiss()
            hide()
        }

        overlayView.snooze.setOnClickListener {
            listener.onSnooze()
            hide()
        }

        overlayView.done.setOnClickListener {
            listener.onDone()
            hide()
        }
    }

    private fun addViewToWindowManager(view: ViewGroup) {
        val focusable = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED.
            or(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN).
            or(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL).
            or(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            BaseOverlayViewController.WindowOverlayCompat.TYPE_SYSTEM_ERROR,
            focusable,
            PixelFormat.TRANSLUCENT)

        windowManager.addView(view, layoutParams)
    }

    private fun removeViewFromWindowManager(view: ViewGroup) {
        windowManager.removeViewImmediate(view)
    }

    private fun hide() {
        startHideAnimation(overlayView, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                removeViewFromWindowManager(overlayView)
            }
        })
    }

    private fun startShowAnimation(view: ViewGroup) {
        playShowBackgroundAnimation(view)
        playShowPetAnimation(view)
    }

    private fun playShowPetAnimation(view: ViewGroup) {
        val petSet = AnimatorSet()
        val petAnimator = createShowPetAnimator(view.pet)
        val petStateAnimator = createShowPetAnimator(view.petState)
        petSet.playTogether(petAnimator, petStateAnimator)
        petSet.startDelay = 300
        petSet.start()
    }

    private fun playShowBackgroundAnimation(view: ViewGroup) {
        val props = calculateRevealAnimationProperties(view.backgroundView)

        val backgroundAnim = ViewAnimationUtils.createCircularReveal(
            view.backgroundView,
            props.centerX, props.centerY,
            0f, props.radius
        )
        backgroundAnim.interpolator = AnticipateInterpolator(2.0f)
        backgroundAnim.duration = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()

        val views = listOf<View>(view.dismiss, view.snooze, view.done,
            view.dismissHint, view.snoozeHint, view.doneHint,
            view.name, view.message, view.time)
        val animators = views.map { ObjectAnimator.ofFloat(it, "alpha", 0f, 1f).setDuration(800) }
            .toMutableList() as MutableList<Animator>
        animators.add(backgroundAnim)

        val set = AnimatorSet()
        set.playTogether(animators)
        set.start()
    }

    private fun startHideAnimation(view: ViewGroup, listener: AnimatorListenerAdapter) {
        view.pet.visibility = View.VISIBLE
        view.petState.visibility = View.VISIBLE

        playHidePetAnimation(view)
        playHideBackgroundAnimation(view, listener)
    }

    private fun playHideBackgroundAnimation(view: ViewGroup, listener: AnimatorListenerAdapter) {
        val props = calculateRevealAnimationProperties(view.backgroundView)
        val backgroundAnim = ViewAnimationUtils.createCircularReveal(
            view.backgroundView,
            props.centerX, props.centerY,
            props.radius, 0f
        )
        backgroundAnim.interpolator = AccelerateDecelerateInterpolator()
        //        anim.duration = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        backgroundAnim.duration = 1000
        backgroundAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.backgroundView.visibility = View.INVISIBLE
            }
        })

        val views = listOf<View>(view.dismiss, view.snooze, view.done,
            view.dismissHint, view.snoozeHint, view.doneHint,
            view.name, view.message, view.time)
        val animators = views.map { ObjectAnimator.ofFloat(it, "alpha", 1f, 0f).setDuration(600) }
            .toMutableList() as MutableList<Animator>
        animators.add(backgroundAnim)

        val set = AnimatorSet()
        set.playTogether(animators)
        set.startDelay = 500
        set.addListener(listener)
        set.start()
    }

    private fun playHidePetAnimation(view: ViewGroup) {
        val petSet = AnimatorSet()
        val petAnimator = createHidePetAnimator(view.pet)
        val petStateAnimator = createHidePetAnimator(view.petState)
        petSet.playTogether(petAnimator, petStateAnimator)
        petSet.start()
    }

    private fun createShowPetAnimator(view: View): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(view, "y", view.y + view.height, view.y)
        animator.duration = view.context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                view.visibility = View.VISIBLE
            }
        })

        return animator
    }

    private fun createHidePetAnimator(view: View): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(view, "y", view.y, view.y + view.height)
        animator.duration = view.context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
//        animator.duration = 1000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                view.visibility = View.INVISIBLE
            }
        })

        return animator
    }

    private fun calculateRevealAnimationProperties(view: View): RevealAnimationProperties {
        val bounds = Rect()
        view.getDrawingRect(bounds)

        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val radius = (getScreenHeight(view.context) - location[1]).toFloat()
        val centerX = bounds.centerX()
        val centerY = radius.toInt() / 2

        return RevealAnimationProperties(centerX, centerY, radius)
    }


    private fun getScreenHeight(context: Context): Int {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    data class RevealAnimationProperties(val centerX: Int, val centerY: Int, val radius: Float)
}