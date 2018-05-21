package io.ipoli.android.common.view.anim

import android.animation.Animator
import android.animation.TimeInterpolator
import android.widget.TextView

class TypewriterTextAnimator private constructor(
    private val textView: TextView,
    private val text: String,
    private val typeSpeed: Int = DEFAULT_TYPE_SPEED,
    private val appendMode: Boolean
) : Animator() {

    private var isRunning = false
    private var textIndex = 0
    private var startDelay = 0L

    private val addCharRunnable = AddCharRunnable()

    companion object {

        private const val DEFAULT_TYPE_SPEED = 25

        fun of(
            textView: TextView,
            text: String,
            typeSpeed: Int = DEFAULT_TYPE_SPEED,
            appendMode: Boolean = true
        ) = TypewriterTextAnimator(textView, text, typeSpeed, appendMode)
    }

    override fun isRunning() = isRunning

    override fun getDuration() = (text.length * typeSpeed).toLong()

    override fun getStartDelay() = startDelay

    override fun setStartDelay(startDelay: Long) {
        this.startDelay = startDelay
    }

    override fun setInterpolator(value: TimeInterpolator) {
        // No support for interpolation
    }

    override fun setDuration(duration: Long): Animator {
        // Do not support duration setting
        return this
    }

    override fun cancel() {
        isRunning = false
        listeners?.toMutableList()?.forEach {
            it.onAnimationCancel(this)
        }
    }

    override fun end() {
        endAnimation()
    }

    private fun endAnimation() {
        isRunning = false
        listeners?.toMutableList()?.forEach {
            it.onAnimationEnd(this)
        }
    }

    override fun start() {
        isRunning = true
        textView.post(addCharRunnable)
    }

    inner class AddCharRunnable : Runnable {
        override fun run() {
            if (!isRunning) {
                return
            }

            if (textIndex >= text.length) {
                endAnimation()
                return
            }

            if (appendMode) {
                textView.append(text, textIndex, textIndex + 1)
            } else {
                textView.text = text
            }

            textIndex++
            textView.postDelayed(addCharRunnable, typeSpeed.toLong())
        }
    }
}