package io.ipoli.android.common.ui

import android.support.annotation.ColorRes
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/26/17.
 */
enum class Color(@ColorRes val color200: Int) {
    RED(R.color.md_red_200),
    GREEN(R.color.md_green_200),
    BLUE(R.color.md_blue_200),
    PURPLE(R.color.md_purple_200),
    BROWN(R.color.md_brown_200),
    ORANGE(R.color.md_orange_200),
    PINK(R.color.md_pink_200),
    TEAL(R.color.md_teal_200),
    DEEP_ORANGE(R.color.md_deep_orange_200),
    INDIGO(R.color.md_indigo_200),
    BLUE_GREY(R.color.md_blue_grey_200),
    LIME(R.color.md_lime_200);
}