package io.ipoli.android.common

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/24/17.
 */
object ViewUtils {

    fun dpToPx(dips: Float, context: Context): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.resources.displayMetrics)

    fun spToPx(sp: Int, context: Context): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics).toInt()

    fun pxToDp(px: Int, context: Context): Float =
        px / context.resources.displayMetrics.density

    fun showViews(vararg views: View) =
        views.forEach { it.visibility = View.VISIBLE }

    fun hideViews(vararg views: View) =
        views.forEach { it.visibility = View.INVISIBLE }

    fun goneViews(vararg views: View) =
        views.forEach { it.visibility = View.GONE }

    fun hideKeyboard(view: View) {
        val m = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        m.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }

    fun setMarginTop(view: View, marginDp: Int) {
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        lp.topMargin = dpToPx(marginDp.toFloat(), view.context).toInt()
        view.layoutParams = lp
    }

    fun setMarginBottom(view: View, marginDp: Int) {
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        lp.bottomMargin = dpToPx(marginDp.toFloat(), view.context).toInt()
        view.layoutParams = lp
    }
}