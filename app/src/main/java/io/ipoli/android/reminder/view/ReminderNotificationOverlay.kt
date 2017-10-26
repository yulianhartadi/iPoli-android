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
            //                        startShowAnimation(view)
            startHideAnimation(view)
        }
    }

    data class RevealAnimationProperties(val centerX: Int, val centerY: Int, val radius: Float)

    private fun startShowAnimation(view: ViewGroup) {

        val props = calculateRevealAnimationProperties(view.backgroundView)

        val anim = ViewAnimationUtils.createCircularReveal(
            view.backgroundView,
            props.centerX, props.centerY,
            0f, props.radius
        )
        anim.interpolator = AnticipateInterpolator(2.0f)
        anim.duration = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        anim.start()

        val petAnimator = createShowPetAnimator(view.pet)
        val petStateAnimator = createShowPetAnimator(view.petState)
        petAnimator.start()
        petStateAnimator.start()
    }

    private fun startHideAnimation(view: ViewGroup) {
        view.pet.visibility = View.VISIBLE
        view.petState.visibility = View.VISIBLE

        val petAnimator = createHidePetAnimator(view.pet)
        val petStateAnimator = createHidePetAnimator(view.petState)
        petAnimator.start()
        petStateAnimator.start()

        val props = calculateRevealAnimationProperties(view.backgroundView)
        val backgroundAnim = ViewAnimationUtils.createCircularReveal(
            view.backgroundView,
            props.centerX, props.centerY,
            props.radius, 0f
        )
        backgroundAnim.interpolator = AccelerateDecelerateInterpolator()
//        anim.duration = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        backgroundAnim.duration = 2000
        backgroundAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.backgroundView.visibility = View.INVISIBLE
            }
        })


        val views = listOf<View>(view.dismiss, view.snooze, view.done,
            view.dismissHint, view.snoozeHint, view.doneHint,
            view.name, view.message, view.time)
        val animators = views.map { ObjectAnimator.ofFloat(it, "alpha", 1f, 0f).setDuration(1000) }
            .toMutableList() as MutableList<Animator>
        animators.add(backgroundAnim)

        val set = AnimatorSet()
        set.playTogether(animators)
        set.startDelay = 500
        set.start()


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

    private fun createShowPetAnimator(view: View): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(view, "y", view.y + view.height, view.y)
        animator.duration = view.context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.startDelay = 300
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                view.visibility = View.VISIBLE
            }
        })

        return animator
    }

    private fun createHidePetAnimator(view: View): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(view.pet, "y", view.y, view.y + view.height)
//        animator.duration = view.context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        animator.duration = 1000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                view.visibility = View.INVISIBLE
            }
        })

        return animator
    }
}