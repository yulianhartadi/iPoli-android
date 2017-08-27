package io.ipoli.android.common.ui

import android.view.View

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/17.
 */
var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }