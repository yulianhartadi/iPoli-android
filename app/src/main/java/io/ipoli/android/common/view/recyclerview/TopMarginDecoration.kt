package io.ipoli.android.common.view.recyclerview

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import io.ipoli.android.common.ViewUtils

class TopMarginDecoration(private val topMargin: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = ViewUtils.dpToPx(topMargin.toFloat(), view.context).toInt()
    }
}