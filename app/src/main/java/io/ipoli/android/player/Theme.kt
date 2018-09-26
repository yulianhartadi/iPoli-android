package io.ipoli.android.player

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/12/17.
 */
enum class Theme(val gemPrice: Int, val isDark: Boolean) {
    RED(3, false),
    BLUE_GREY(1, false),
    ORANGE(3, false),
    PINK(6, false),
    GREEN(3, false),
    BLUE(3, false),
    PURPLE(4, false),
    DARK_PURPLE(5, true),
    DARK_BLUE(7, true),
    DARK_BLUE_GREY(4, true),
    DARK_TEAL(9, true),
    DARK_ORANGE(2, true),
    DARK_RED(10, true),
    DARK_PINK(8, true),
    DARK_DEEP_PURPLE(9, true),
    BLACK_TEAL(12, true),
    BLACK_RED(12, true),
    BLACK_BLUE(12, true)
}