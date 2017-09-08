package io.ipoli.android.common

import android.content.Context
import android.util.TypedValue


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/24/17.
 */
object ViewUtils {

    fun dpToPx(dips: Float, context: Context): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.resources.displayMetrics)
}