package mypoli.android.quest.data

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes

import mypoli.android.R

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/20/17.
 */
enum class Category(@param:ColorRes val color50: Int,
                    @param:ColorRes val color100: Int,
                    @param:ColorRes val color300: Int,
                    @param:ColorRes val color500: Int,
                    @param:ColorRes val color700: Int,
                    @ColorRes val color800: Int,
                    @DrawableRes val whiteImage: Int,
                    @DrawableRes val colorfulImage: Int) {

    LEARNING(R.color.md_blue_50, R.color.md_blue_100, R.color.md_blue_300, R.color.md_blue_500, R.color.md_blue_700, R.color.md_blue_900, R.drawable.ic_context_learning_white_24dp, R.drawable.ic_context_learning_blue_24dp),
    WELLNESS(R.color.md_green_50, R.color.md_green_100, R.color.md_green_300, R.color.md_green_500, R.color.md_green_700, R.color.md_green_800, R.drawable.ic_icon_white_24dp, R.drawable.ic_context_wellness_green_24dp),
    PERSONAL(R.color.md_orange_50, R.color.md_orange_100, R.color.md_orange_300, R.color.md_orange_500, R.color.md_orange_700, R.color.md_orange_800, R.drawable.ic_context_personal_white_24dp, R.drawable.ic_context_personal_orange_24dp),
    WORK(R.color.md_red_50, R.color.md_red_100, R.color.md_red_300, R.color.md_red_500, R.color.md_red_700, R.color.md_red_800, R.drawable.ic_context_work_white, R.drawable.ic_context_work_red),
    FUN(R.color.md_purple_50, R.color.md_purple_100, R.color.md_purple_300, R.color.md_purple_500, R.color.md_purple_700, R.color.md_purple_900, R.drawable.ic_context_fun_white, R.drawable.ic_context_fun_purple),
    CHORES(R.color.md_brown_50, R.color.md_brown_100, R.color.md_brown_300, R.color.md_brown_500, R.color.md_brown_700, R.color.md_brown_800, R.drawable.ic_context_chores_white, R.drawable.ic_context_chores_brown);

    companion object {
        fun getNameRes(category: Category): Int =
            when (category) {
                LEARNING -> R.string.category_learning
                WELLNESS -> R.string.category_wellness
                PERSONAL -> R.string.category_personal
                WORK -> R.string.category_work
                FUN -> R.string.category_fun
                else -> R.string.category_chores
            }
    }
}
