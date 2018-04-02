package io.ipoli.android.common.view.recyclerview

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

class ReorderItemHelper(
    private val onItemMoved: (Int, Int) -> Unit = { _, _ -> },
    private val onItemReordered: (Int, Int) -> Unit = { _, _ -> }
) :
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

    private var dragFrom = -1
    private var dragTo = -1

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPos = viewHolder.adapterPosition
        val toPos = target.adapterPosition

        if (dragFrom == -1) {
            dragFrom = fromPos
        }
        dragTo = toPos

        onItemMoved(fromPos, toPos)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
            onItemReordered(dragFrom, dragTo)
        }

        dragFrom = -1
        dragTo = -1
    }
}