package io.ipoli.android.common.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */
class EndlessRecyclerViewScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val loadDataCallback: (Side, Int) -> Unit,
    private val visibleThreshold: Int = 5
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = layoutManager.itemCount

        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (lastVisibleItemPosition + visibleThreshold > totalItemCount) loadDataCallback(
            Side.BOTTOM,
            firstVisibleItemPosition
        )
        else if (firstVisibleItemPosition - visibleThreshold < 0) loadDataCallback(
            Side.TOP,
            firstVisibleItemPosition
        )
    }

    enum class Side { TOP, BOTTOM }
}