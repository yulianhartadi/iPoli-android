package io.ipoli.android.common.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.RelativeLayout
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils

class PopupBackgroundLayout : RelativeLayout {
    private var onBackPressed: () -> Unit = {}

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            onBackPressed()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    fun setOnBackPressed(action: () -> Unit) {
        onBackPressed = action
    }
}

abstract class BasePopup(private val isAutoHide: Boolean = false) {

    private lateinit var overlayView: PopupBackgroundLayout
    private lateinit var contentView: ViewGroup
    private lateinit var windowManager: WindowManager
    protected lateinit var activity: Context

    private val autoHideRunnable = {
        hide()
    }

    abstract fun createView(inflater: LayoutInflater): View

    fun show(context: Context) {
        activity = context
        contentView = createView(LayoutInflater.from(context)) as ViewGroup
        contentView.visibility = View.INVISIBLE

        overlayView = PopupBackgroundLayout(context)
        overlayView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        overlayView.setBackgroundResource(R.color.md_dark_text_12)

        val contentLp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        contentLp.marginStart = ViewUtils.dpToPx(24f, context).toInt()
        contentLp.marginEnd = ViewUtils.dpToPx(24f, context).toInt()
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

        overlayView.addView(contentView, contentLp)

        overlayView.setOnBackPressed {
            if (!isAutoHide) {
                hide()
            }
        }
        if (!isAutoHide) {
            overlayView.setOnClickListener { hide() }
        }

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        addViewToWindowManager(overlayView)
        overlayView.post {
            playEnterAnimation(contentView)
        }
    }

    protected open fun playEnterAnimation(contentView: View) {
        val transAnim = ObjectAnimator.ofFloat(contentView, "y", getScreenHeight(contentView.context).toFloat(), contentView.y)
        val fadeAnim = ObjectAnimator.ofFloat(contentView, "alpha", 0f, 1f)
        transAnim.duration = contentView.context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        fadeAnim.duration = contentView.context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        val animSet = AnimatorSet()
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                contentView.visible = true
            }

            override fun onAnimationEnd(animation: Animator) {
                onViewShown(contentView)
            }
        })
        animSet.playTogether(transAnim, fadeAnim)
        animSet.start()
    }

    protected open fun onViewShown(contentView: View) {

    }

    protected open fun playExitAnimation(contentView: View) {
        val transAnim = ObjectAnimator.ofFloat(contentView, "y", contentView.y, getScreenHeight(contentView.context).toFloat())
        val fadeAnim = ObjectAnimator.ofFloat(contentView, "alpha", 1f, 0f)
        transAnim.duration = contentView.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        fadeAnim.duration = contentView.context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        val animSet = AnimatorSet()
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                windowManager.removeViewImmediate(overlayView)
            }
        })
        animSet.playTogether(transAnim, fadeAnim)
        animSet.start()
    }

    private fun addViewToWindowManager(view: ViewGroup) {
        val focusable = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED.
            or(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN).
            or(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

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

    protected fun getScreenHeight(context: Context): Int {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    protected fun autoHideAfter(millis: Long) {
        require(isAutoHide)
        contentView.postDelayed(autoHideRunnable, millis)
    }

    fun hide() {
        contentView.removeCallbacks(autoHideRunnable)
        overlayView.setOnClickListener(null)
        overlayView.isClickable = false
        playExitAnimation(contentView)
    }
}