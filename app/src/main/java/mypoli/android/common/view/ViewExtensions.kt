package mypoli.android.common.view

import android.support.constraint.Group
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

fun Group.views() =
    referencedIds.map { id ->
        rootView.findViewById<View>(id)
    }

fun Group.goneViews() {
    this.visibility = View.GONE
}

fun Group.hideViews() {
    this.visibility = View.INVISIBLE
}

fun Group.showViews() {
    this.visibility = View.VISIBLE
}