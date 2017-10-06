package io.ipoli.android.common.ui

import android.support.annotation.ColorRes
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/26/17.
 */
enum class Color(@ColorRes val color200: Int, @ColorRes val color500: Int) {
    RED(R.color.md_red_200, R.color.md_red_500),
    GREEN(R.color.md_green_200, R.color.md_green_500),
    BLUE(R.color.md_blue_200, R.color.md_blue_500),
    PURPLE(R.color.md_purple_200, R.color.md_purple_500),
    BROWN(R.color.md_brown_200, R.color.md_brown_500),
    ORANGE(R.color.md_orange_200, R.color.md_orange_500),
    PINK(R.color.md_pink_200, R.color.md_pink_500),
    TEAL(R.color.md_teal_200, R.color.md_teal_500),
    DEEP_ORANGE(R.color.md_deep_orange_200, R.color.md_deep_orange_500),
    INDIGO(R.color.md_indigo_200, R.color.md_indigo_500),
    BLUE_GREY(R.color.md_blue_grey_200, R.color.md_blue_grey_500),
    LIME(R.color.md_lime_200, R.color.md_lime_500);
}