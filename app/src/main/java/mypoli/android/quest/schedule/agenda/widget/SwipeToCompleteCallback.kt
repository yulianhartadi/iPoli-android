package mypoli.android.quest.schedule.agenda.widget

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
    @ColorRes swipeBackgroundColor: Int
) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {

    private val swipeIcon = ContextCompat.getDrawable(context, completeIcon)!!
    private val intrinsicWidth = swipeIcon.intrinsicWidth
    private val intrinsicHeight = swipeIcon.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = ContextCompat.getColor(context, swipeBackgroundColor)

    override fun onMove(
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder?,
        target: RecyclerView.ViewHolder?
    ) = false

    override fun getSwipeDirs(
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder?
    ): Int {
        return if (canSwipe(recyclerView, viewHolder)) {
            super.getSwipeDirs(recyclerView, viewHolder)
        } else 0
    }

    abstract fun canSwipe(
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder?
    ): Boolean

    override fun onChildDraw(
        c: Canvas?,
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        drawBackground(viewHolder.itemView, dX, c)
        drawIcon(viewHolder.itemView, c)
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun drawBackground(
        itemView: View,
        dX: Float,
        c: Canvas?
    ) {
        background.color = backgroundColor
        background.setBounds(
            itemView.left,
            itemView.top,
            itemView.left + dX.toInt(),
            itemView.bottom
        )
        background.draw(c)
    }

    private fun drawIcon(
        itemView: View,
        c: Canvas?
    ) {

        val itemHeight = itemView.bottom - itemView.top

        val margin = (itemHeight - intrinsicHeight) / 2
        val top = itemView.top + (itemHeight - intrinsicHeight) / 2
        val left = itemView.left + margin - intrinsicWidth
        val right = itemView.left + margin
        val bottom = top + intrinsicHeight

        swipeIcon.setBounds(left, top, right, bottom)
        swipeIcon.draw(c)
    }
}