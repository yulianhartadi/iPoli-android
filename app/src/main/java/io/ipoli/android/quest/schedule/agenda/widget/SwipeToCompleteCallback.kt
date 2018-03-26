package io.ipoli.android.quest.schedule.agenda.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
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
abstract class SwipeToCompleteCallback(
    context: Context,
    @DrawableRes completeIcon: Int,
    @ColorRes completeBackground: Int,
    @DrawableRes undoCompletedIcon: Int,
    @ColorRes undoCompletedBackground: Int
) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {

    companion object {
        const val ALPHA_FULL = 1.0f
        const val ALPHA_MAGNIFICATION = .8f
    }

    private val completeSwipeIcon = ContextCompat.getDrawable(context, completeIcon)!!
    private val completeBackgroundColor = ContextCompat.getColor(context, completeBackground)
    private val undoCompletedSwipeIcon = ContextCompat.getDrawable(context, undoCompletedIcon)!!
    private val undoCompletedBackgroundColor =
        ContextCompat.getColor(context, undoCompletedBackground)
    private val intrinsicWidth = completeSwipeIcon.intrinsicWidth
    private val intrinsicHeight = completeSwipeIcon.intrinsicHeight
    private val background = ColorDrawable()

    override fun onMove(
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder?,
        target: RecyclerView.ViewHolder?
    ) = false

    override fun onChildDraw(
        c: Canvas?,
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        val alpha = ALPHA_FULL - (Math.abs(dX) / viewHolder.itemView.width.toFloat()) * ALPHA_MAGNIFICATION
        if (dX > 0) {
            drawCompleteBackground(viewHolder.itemView, dX, c)
            drawCompleteIcon(viewHolder.itemView, c)
        } else {
            drawUndoCompleteBackground(viewHolder.itemView, dX, c)
            drawUndoCompleteIcon(viewHolder.itemView, c)
        }
        viewHolder.itemView.alpha = alpha

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun drawUndoCompleteIcon(itemView: View, c: Canvas?) {

        val itemHeight = itemView.bottom - itemView.top

        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        undoCompletedSwipeIcon.setBounds(
            deleteIconLeft,
            deleteIconTop,
            deleteIconRight,
            deleteIconBottom
        )
        undoCompletedSwipeIcon.draw(c)
    }

    private fun drawUndoCompleteBackground(itemView: View, dX: Float, c: Canvas?) {
        background.color = undoCompletedBackgroundColor
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(c)
    }

    private fun drawCompleteBackground(
        itemView: View,
        dX: Float,
        c: Canvas?
    ) {
        background.color = completeBackgroundColor
        background.setBounds(
            itemView.left,
            itemView.top,
            itemView.left + dX.toInt(),
            itemView.bottom
        )
        background.draw(c)
    }

    private fun drawCompleteIcon(
        itemView: View,
        c: Canvas?
    ) {
        val itemHeight = itemView.bottom - itemView.top

        val margin = (itemHeight - intrinsicHeight) / 2
        val top = itemView.top + (itemHeight - intrinsicHeight) / 2
        val left = itemView.left + margin - intrinsicWidth
        val right = itemView.left + margin
        val bottom = top + intrinsicHeight

        completeSwipeIcon.setBounds(left, top, right, bottom)
        completeSwipeIcon.draw(c)
    }
}