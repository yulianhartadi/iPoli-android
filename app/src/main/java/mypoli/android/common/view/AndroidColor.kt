package mypoli.android.common.view

import android.support.annotation.ColorRes
import mypoli.android.R

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/26/17.
 */
enum class AndroidColor(@ColorRes val color200: Int,
                        @ColorRes val color500: Int,
                        @ColorRes val color600: Int,
                        @ColorRes val color700: Int,
                        @ColorRes val color900: Int) {
    RED(
        R.color.md_red_200,
        R.color.md_red_500,
        R.color.md_red_600,
        R.color.md_red_700,
        R.color.md_red_900),

    GREEN(
        R.color.md_green_200,
        R.color.md_green_500,
        R.color.md_green_600,
        R.color.md_green_700,
        R.color.md_green_900),

    BLUE(
        R.color.md_blue_200,
        R.color.md_blue_500,
        R.color.md_blue_600,
        R.color.md_blue_700,
        R.color.md_blue_900),

    PURPLE(
        R.color.md_purple_200,
        R.color.md_purple_500,
        R.color.md_purple_600,
        R.color.md_purple_700,
        R.color.md_purple_900),

    BROWN(
        R.color.md_brown_200,
        R.color.md_brown_500,
        R.color.md_brown_600,
        R.color.md_brown_700,
        R.color.md_brown_900),

    ORANGE(
        R.color.md_orange_200,
        R.color.md_orange_500,
        R.color.md_orange_600,
        R.color.md_orange_700,
        R.color.md_orange_900),

    PINK(
        R.color.md_pink_200,
        R.color.md_pink_500,
        R.color.md_pink_600,
        R.color.md_pink_700,
        R.color.md_pink_900),

    TEAL(
        R.color.md_teal_200,
        R.color.md_teal_500,
        R.color.md_teal_600,
        R.color.md_teal_700,
        R.color.md_teal_900),

    DEEP_ORANGE(
        R.color.md_deep_orange_200,
        R.color.md_deep_orange_500,
        R.color.md_deep_orange_600,
        R.color.md_deep_orange_700,
        R.color.md_deep_orange_900),

    INDIGO(
        R.color.md_indigo_200,
        R.color.md_indigo_500,
        R.color.md_indigo_600,
        R.color.md_indigo_700,
        R.color.md_indigo_900),

    BLUE_GREY(
        R.color.md_blue_grey_200,
        R.color.md_blue_grey_500,
        R.color.md_blue_grey_600,
        R.color.md_blue_grey_700,
        R.color.md_blue_grey_900),

    LIME(
        R.color.md_lime_200,
        R.color.md_lime_600,
        R.color.md_lime_700,
        R.color.md_lime_800,
        R.color.md_lime_900);
}