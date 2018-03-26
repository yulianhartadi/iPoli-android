package io.ipoli.android.common.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/31/2018.
 */
class ChangeItemScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val visibleItemCallback: (Int) -> Unit
) : RecyclerView.OnScrollListener() {

    private var lastVisibleItemPos: Int? = null

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        val currentVisibleItemPos = layoutManager.findFirstVisibleItemPosition()

        lastVisibleItemPos?.let {
            if (it != currentVisibleItemPos) {
                visibleItemCallback(currentVisibleItemPos)
            }
        } ?: visibleItemCallback(currentVisibleItemPos)
    }
}