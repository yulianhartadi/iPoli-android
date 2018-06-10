package io.ipoli.android.quest.view

import android.animation.*
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.DrawableRes
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.OvershootInterpolator
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.anim.TypewriterTextAnimator
import io.ipoli.android.common.view.visible
import io.ipoli.android.pet.Food
import kotlinx.android.synthetic.main.popup_quest_complete.view.*
import java.util.*

class QuestCompletePopup(
    @DrawableRes private val petHeadImage: Int,
    private val earnedXP: Int,
    private val earnedCoins: Int,
    private val bounty: Food? = null
) : ToastOverlay() {


    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_quest_complete, null)

    override fun onViewShown(contentView: View) {
        contentView.pet.setImageResource(petHeadImage)
        bounty?.let {
            contentView.bounty.setImageResource(it.image)
        }
        startTypingAnimation(contentView)
    }

    private fun startTypingAnimation(contentView: View) {
        val title = contentView.message
        val messages = contentView.resources.getStringArray(R.array.quest_complete_message)
        val message = messages[Random().nextInt(messages.size)]
        val typewriterAnim = TypewriterTextAnimator.of(
            title,
            message,
            typeSpeed = TypewriterTextAnimator.DEFAULT_TYPE_SPEED - 5
        )
        typewriterAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startEarnedRewardAnimation(contentView)
            }
        })
        typewriterAnim.start()
    }

    private fun startEarnedRewardAnimation(contentView: View) {
        val earnedXP = contentView.earnedXP
        val earnedCoins = contentView.earnedCoins

        val xpAnim = ValueAnimator.ofInt(0, this.earnedXP)
        xpAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                earnedXP.visible = true
            }
        })
        xpAnim.addUpdateListener {
            earnedXP.text = "${it.animatedValue}"
        }

        val coinsAnim = ValueAnimator.ofInt(0, this.earnedCoins)

        coinsAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                earnedCoins.visible = true
            }
        })

        coinsAnim.addUpdateListener {
            earnedCoins.text = "${it.animatedValue}"
        }

        val anim = AnimatorSet()
        anim.duration = 300
        anim.playSequentially(xpAnim, coinsAnim)

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {

                if (bounty != null) playRewardAnimation(contentView)
                else autoHideAfter(700)
            }
        })

        anim.start()
    }

    private fun playRewardAnimation(contentView: View) {

        val alphaSet = AnimatorSet()
        alphaSet.playTogether(
            ObjectAnimator.ofFloat(contentView.earnedCoins, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(contentView.earnedXP, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(contentView.message, "alpha", 1f, 0f)
        )
        alphaSet.startDelay = 500

        alphaSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                playBountyAnimation(contentView)
            }
        })

        alphaSet.start()
    }

    private fun playBountyAnimation(contentView: View) {
        val qAnim = ObjectAnimator.ofFloat(contentView.bountyQuantity, "alpha", 0f, 1f)
        val xAnim = ObjectAnimator.ofFloat(contentView.bounty, "scaleX", 0f, 1f)
        val yAnim = ObjectAnimator.ofFloat(contentView.bounty, "scaleY", 0f, 1f)
        val bountyAnim = AnimatorSet()
        bountyAnim.interpolator = OvershootInterpolator()
        bountyAnim.playTogether(qAnim, xAnim, yAnim)

        bountyAnim.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                contentView.bounty.visible = true
                contentView.bountyQuantity.visible = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                autoHideAfter(700)
            }
        })
        bountyAnim.start()
    }
}


abstract class ToastOverlay {

    private lateinit var contentView: ViewGroup
    private lateinit var windowManager: WindowManager
    private val displayMetrics = DisplayMetrics()
    private val autoHideHandler = Handler(Looper.getMainLooper())

    private val autoHideRunnable = {
        hide()
    }

    abstract fun createView(inflater: LayoutInflater): View

    fun show(context: Context) {

        contentView = createView(LayoutInflater.from(context)) as ViewGroup
        contentView.visibility = View.INVISIBLE

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        addViewToWindowManager(contentView)
        contentView.post {
            playEnterAnimation(contentView)
        }
    }

    protected open fun playEnterAnimation(contentView: View) {
        val transAnim = ObjectAnimator.ofFloat(
            contentView,
            "y",
            displayMetrics.heightPixels.toFloat(),
            contentView.y
        )
        val fadeAnim = ObjectAnimator.ofFloat(contentView, "alpha", 0f, 1f)
        transAnim.duration =
            contentView.context.resources.getInteger(android.R.integer.config_shortAnimTime)
                .toLong()
        fadeAnim.duration =
            contentView.context.resources.getInteger(android.R.integer.config_mediumAnimTime)
                .toLong()
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
        val transAnim = ObjectAnimator.ofFloat(
            contentView,
            "y",
            contentView.y,
            displayMetrics.heightPixels.toFloat()
        )
        val fadeAnim = ObjectAnimator.ofFloat(contentView, "alpha", 1f, 0f)
        transAnim.duration =
            contentView.context.resources.getInteger(android.R.integer.config_shortAnimTime)
                .toLong()
        fadeAnim.duration =
            contentView.context.resources.getInteger(android.R.integer.config_mediumAnimTime)
                .toLong()
        val animSet = AnimatorSet()
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onDestroy()
            }
        })
        animSet.playTogether(transAnim, fadeAnim)
        animSet.start()
    }

    private fun onDestroy() {
        windowManager.removeViewImmediate(contentView)
    }

    private fun addViewToWindowManager(view: ViewGroup) {
        val focusable =
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED.or(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                .or(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

        val width = displayMetrics.widthPixels - (ViewUtils.dpToPx(32f, view.context).toInt())

        val layoutParams = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowOverlayCompat.TYPE_SYSTEM_ERROR,
            focusable,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.CENTER_HORIZONTAL.or(Gravity.BOTTOM)

        layoutParams.y += ViewUtils.dpToPx(24f, view.context).toInt()

        windowManager.addView(view, layoutParams)
    }

    protected fun autoHideAfter(millis: Long) {
        autoHideHandler.postDelayed(autoHideRunnable, millis)
    }

    fun hide() {
        autoHideHandler.removeCallbacksAndMessages(null)
        contentView.setOnClickListener(null)
        playExitAnimation(contentView)
    }

    internal object WindowOverlayCompat {
        private const val ANDROID_OREO = 26
        private const val TYPE_APPLICATION_OVERLAY = 2038

        @Suppress("DEPRECATION")
        val TYPE_SYSTEM_ERROR =
            if (Build.VERSION.SDK_INT < ANDROID_OREO) WindowManager.LayoutParams.TYPE_SYSTEM_ERROR else TYPE_APPLICATION_OVERLAY
    }
}