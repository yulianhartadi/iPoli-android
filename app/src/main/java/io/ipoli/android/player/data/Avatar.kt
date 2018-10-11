package io.ipoli.android.player.data

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import io.ipoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 8/21/17.
 */
enum class Avatar(val gemPrice: Int) {
    AVATAR_00(0),
    AVATAR_01(8),
    AVATAR_02(5),
    AVATAR_03(1),
    AVATAR_04(1),
    AVATAR_05(5),
    AVATAR_06(5),
    AVATAR_07(8),
    AVATAR_08(5),
    AVATAR_09(1),
    AVATAR_10(8),
    AVATAR_11(5),
    AVATAR_12(5),
    AVATAR_13(5),
    AVATAR_14(8),
    AVATAR_15(8),
    AVATAR_16(1),
    AVATAR_17(8),
    AVATAR_18(8),
    AVATAR_19(8),
    AVATAR_20(8);
//    AVATAR_21(8),
//    AVATAR_22(8),
//    AVATAR_23(8),
//    AVATAR_24(8),
//    AVATAR_25(8),
//    AVATAR_26(8),
//    AVATAR_27(8),
//    AVATAR_28(8),
//    AVATAR_29(8),
//    AVATAR_30(8);
}

enum class AndroidAvatar(
    @StringRes val avatarName: Int,
    @DrawableRes val image: Int,
    @ColorRes val backgroundColor: Int
) {
    AVATAR_00(R.string.avatar_name_00, R.drawable.avatar_00, R.color.md_purple_300),
    AVATAR_01(R.string.avatar_name_01, R.drawable.avatar_01, R.color.md_red_300),
    AVATAR_02(R.string.avatar_name_02, R.drawable.avatar_02, R.color.md_blue_300),
    AVATAR_03(R.string.avatar_name_03, R.drawable.avatar_03, R.color.md_red_300),
    AVATAR_04(R.string.avatar_name_04, R.drawable.avatar_04, R.color.md_blue_300),
    AVATAR_05(R.string.avatar_name_05, R.drawable.avatar_05, R.color.md_red_300),
    AVATAR_06(R.string.avatar_name_06, R.drawable.avatar_06, R.color.md_blue_300),
    AVATAR_07(R.string.avatar_name_07, R.drawable.avatar_07, R.color.md_red_300),
    AVATAR_08(R.string.avatar_name_08, R.drawable.avatar_08, R.color.md_blue_300),
    AVATAR_09(R.string.avatar_name_09, R.drawable.avatar_09, R.color.md_blue_300),
    AVATAR_10(R.string.avatar_name_10, R.drawable.avatar_10, R.color.md_red_300),
    AVATAR_11(R.string.avatar_name_11, R.drawable.avatar_11, R.color.md_blue_300),
    AVATAR_12(R.string.avatar_name_12, R.drawable.avatar_12, R.color.md_blue_300),
    AVATAR_13(R.string.avatar_name_13, R.drawable.avatar_13, R.color.md_red_300),
    AVATAR_14(R.string.avatar_name_14, R.drawable.avatar_14, R.color.md_red_300),
    AVATAR_15(R.string.avatar_name_15, R.drawable.avatar_15, R.color.md_blue_300),
    AVATAR_16(R.string.avatar_name_16, R.drawable.avatar_16, R.color.md_red_300),
    AVATAR_17(R.string.avatar_name_17, R.drawable.avatar_17, R.color.md_red_300),
    AVATAR_18(R.string.avatar_name_18, R.drawable.avatar_18, R.color.md_blue_300),
    AVATAR_19(R.string.avatar_name_19, R.drawable.avatar_19, R.color.md_red_300),
    AVATAR_20(R.string.avatar_name_20, R.drawable.avatar_20, R.color.md_red_300),
    AVATAR_21(R.string.avatar_name_21, R.drawable.avatar_21, R.color.md_blue_300),
    AVATAR_22(R.string.avatar_name_22, R.drawable.avatar_22, R.color.md_red_300),
    AVATAR_23(R.string.avatar_name_23, R.drawable.avatar_23, R.color.md_red_300),
    AVATAR_24(R.string.avatar_name_24, R.drawable.avatar_24, R.color.md_blue_300),
    AVATAR_25(R.string.avatar_name_25, R.drawable.avatar_25, R.color.md_red_300),
    AVATAR_26(R.string.avatar_name_26, R.drawable.avatar_26, R.color.md_red_300),
    AVATAR_27(R.string.avatar_name_27, R.drawable.avatar_27, R.color.md_blue_300),
    AVATAR_28(R.string.avatar_name_28, R.drawable.avatar_28, R.color.md_blue_300),
    AVATAR_29(R.string.avatar_name_29, R.drawable.avatar_29, R.color.md_blue_300),
    AVATAR_30(R.string.avatar_name_30, R.drawable.avatar_30, R.color.md_blue_300);
}