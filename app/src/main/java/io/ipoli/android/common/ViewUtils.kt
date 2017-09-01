package io.ipoli.android.common

import android.content.res.Resources
import android.util.TypedValue


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/24/17.
 */
object ViewUtils {

    fun dpToPx(dips: Float, resources: Resources): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, resources.displayMetrics)
}