package io.ipoli.android.common.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler
import io.ipoli.android.quest.calendar.CalendarViewController

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/13/17.
 */
abstract class BaseOverlayViewController : Controller() {

    private lateinit var overlayView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        overlayView = createOverlayView(inflater)

        val focusable = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED.
            or(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN).
            or(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL).
            or(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val layoutParams = WindowManager.LayoutParams(
            (metrics.widthPixels * 0.95f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            CalendarViewController.WindowOverlayCompat.TYPE_SYSTEM_ERROR,
            focusable,
            PixelFormat.TRANSLUCENT)
        layoutParams.y = 800

        overlayView.alpha = 0f
        windowManager.addView(overlayView, layoutParams)

        val moveAnimator = ValueAnimator.ofInt(layoutParams.y + 100, layoutParams.y)
        moveAnimator.addUpdateListener { animator ->
            val yPos = animator.animatedValue as Int
            val params = overlayView.layoutParams as WindowManager.LayoutParams
            params.y = yPos
            windowManager.updateViewLayout(overlayView, params)
        }
        moveAnimator.duration = 200

        val animatorSet = AnimatorSet()
        val scale = ObjectAnimator.ofFloat(overlayView, View.SCALE_X, 0.2f, 1f).setDuration(200)
        scale.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(overlayView, View.ALPHA, 0f, 1f).setDuration(500),
            scale,
            moveAnimator
        )
        animatorSet.startDelay = 500
        animatorSet.start()

        overlayView.postDelayed({
            router.popController(this)
        }, 3000)

        return View(activity!!)
    }

    abstract fun createOverlayView(inflater: LayoutInflater): View

    override fun onDestroyView(view: View) {
        windowManager.removeView(overlayView)
        super.onDestroyView(view)
    }

    fun show(router: Router) {
        router.pushController(RouterTransaction.with(this)
            .pushChangeHandler(SimpleSwapChangeHandler(false))
            .popChangeHandler(SimpleSwapChangeHandler(false)))
    }

    private val windowManager by lazy { activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

}