package io.ipoli.android.common.view

import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/12/17.
 */
enum class AndroidTheme(@StringRes val title: Int, @StyleRes val style: Int) {
    RED(R.string.theme_red, R.style.Theme_myPoli_Red),
    ORANGE(R.string.theme_orange, R.style.Theme_myPoli_Orange),
    PINK(R.string.theme_pink, R.style.Theme_myPoli_Pink),
    GREEN(R.string.theme_green, R.style.Theme_myPoli_Green),
    BLUE(R.string.theme_blue, R.style.Theme_myPoli_Blue),
    PURPLE(R.string.theme_purple, R.style.Theme_myPoli_Purple),
    BLUE_GREY(R.string.theme_blue_grey, R.style.Theme_myPoli_Blue_Grey)
}