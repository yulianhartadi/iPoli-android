package mypoli.android.common.view

import android.view.View

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/27/17.
 */
var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

fun View.setScale(scale: Float) {
    scaleX = scale
    scaleY = scale
}