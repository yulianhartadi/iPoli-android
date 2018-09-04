package io.ipoli.android.common.view.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/31/2018.
 */
data class SwipeResource(@DrawableRes val iconRes: Int, @ColorRes val colorRes: Int) {

    fun getIconDrawable(context: Context) = ContextCompat.getDrawable(context, iconRes)!!

    fun getColor(context: Context) = ContextCompat.getColor(context, colorRes)
}

abstract class SwipeCallback :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {

    companion object {
        const val ALPHA_FULL = 1.0f
        const val ALPHA_MAGNIFICATION = .8f
    }

    private val background = ColorDrawable()

    override fun onMove(
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder?,
        target: RecyclerView.ViewHolder?
    ) = false

    abstract fun swipeStartResource(itemViewType: Int): SwipeResource
    abstract fun swipeEndResource(itemViewType: Int): SwipeResource

    override fun onChildDraw(
        c: Canvas?,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        val alpha =
            ALPHA_FULL - (Math.abs(dX) / viewHolder.itemView.width.toFloat()) * ALPHA_MAGNIFICATION
        if (dX > 0) {
            val res = swipeStartResource(viewHolder.itemViewType)
            drawSwipeStartBackground(res.getColor(recyclerView.context), viewHolder.itemView, dX, c)
            drawSwipeStartIcon(res.getIconDrawable(recyclerView.context), viewHolder.itemView, c)
        } else {
            val res = swipeEndResource(viewHolder.itemViewType)
            drawSwipeEndBackground(res.getColor(recyclerView.context), viewHolder.itemView, dX, c)
            drawSwipeEndIcon(res.getIconDrawable(recyclerView.context), viewHolder.itemView, c)
        }
        viewHolder.itemView.alpha = alpha

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun drawSwipeEndIcon(
        icon: Drawable,
        itemView: View,
        c: Canvas?
    ) {

        val itemHeight = itemView.bottom - itemView.top
        val intrinsicHeight = icon.intrinsicHeight
        val intrinsicWidth = icon.intrinsicWidth

        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        icon.setBounds(
            deleteIconLeft,
            deleteIconTop,
            deleteIconRight,
            deleteIconBottom
        )
        icon.draw(c)
    }

    private fun drawSwipeEndBackground(
        color: Int,
        itemView: View,
        dX: Float,
        c: Canvas?
    ) {
        background.color = color
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(c)
    }

    private fun drawSwipeStartBackground(
        color: Int,
        itemView: View,
        dX: Float,
        c: Canvas?
    ) {
        background.color = color
        background.setBounds(
            itemView.left,
            itemView.top,
            itemView.left + dX.toInt(),
            itemView.bottom
        )
        background.draw(c)
    }

    private fun drawSwipeStartIcon(
        icon: Drawable,
        itemView: View,
        c: Canvas?
    ) {
        val itemHeight = itemView.bottom - itemView.top
        val intrinsicHeight = icon.intrinsicHeight
        val intrinsicWidth = icon.intrinsicWidth

        val margin = (itemHeight - intrinsicHeight) / 2
        val top = itemView.top + (itemHeight - intrinsicHeight) / 2
        val left = itemView.left + margin
        val right = itemView.left + margin + intrinsicWidth
        val bottom = top + intrinsicHeight

        icon.setBounds(left, top, right, bottom)
        icon.draw(c)
    }
}

abstract class SimpleSwipeCallback(
    @DrawableRes swipeStartIcon: Int,
    @ColorRes swipeStartBackground: Int,
    @DrawableRes swipeEndIcon: Int,
    @ColorRes swipeEndBackground: Int
) : SwipeCallback() {

    private val swipeResStart = SwipeResource(swipeStartIcon, swipeStartBackground)

    private val swipeResEnd = SwipeResource(swipeEndIcon, swipeEndBackground)

    override fun swipeStartResource(itemViewType: Int) = swipeResStart

    override fun swipeEndResource(itemViewType: Int) = swipeResEnd
}

abstract class MultiViewTypeSwipeCallback(
    private val startResources: Map<Int, SwipeResource>,
    private val endResources: Map<Int, SwipeResource>
) : SwipeCallback() {

    override fun swipeStartResource(itemViewType: Int) =
        startResources[itemViewType]
            ?: throw IllegalStateException("Swiping start viewType with unspecified resource")

    override fun swipeEndResource(itemViewType: Int) =
        endResources[itemViewType]
            ?: throw IllegalStateException("Swiping end viewType with unspecified resource")
}