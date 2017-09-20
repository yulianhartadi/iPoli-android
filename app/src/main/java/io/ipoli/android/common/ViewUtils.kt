package io.ipoli.android.common

import android.content.Context
import android.util.TypedValue
import android.view.View

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/24/17.
 */
object ViewUtils {

    fun dpToPx(dips: Float, context: Context): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.resources.displayMetrics)

    fun pxToDp(px: Int, context: Context): Float =
        px / context.resources.displayMetrics.density

    fun showViews(vararg views: View) =
        views.forEach { it.visibility = View.VISIBLE }

    fun hideViews(vararg views: View) =
        views.forEach { it.visibility = View.GONE }
}