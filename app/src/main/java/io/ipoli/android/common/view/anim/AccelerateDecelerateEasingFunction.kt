package io.ipoli.android.common.view.anim

import android.view.animation.AccelerateDecelerateInterpolator
import com.github.mikephil.charting.animation.Easing

object AccelerateDecelerateEasingFunction : Easing.EasingFunction {

    private val interpolator = AccelerateDecelerateInterpolator()

    override fun getInterpolation(input: Float) =
        interpolator.getInterpolation(input)
}