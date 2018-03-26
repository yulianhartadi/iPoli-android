package io.ipoli.android.player.data

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
    AVATAR_02(8),
    AVATAR_03(8),
    AVATAR_04(8),
    AVATAR_05(8),
    AVATAR_06(8),
    AVATAR_07(8),
    AVATAR_08(8),
    AVATAR_09(8),
    AVATAR_10(8),
    AVATAR_11(8);
}

enum class AndroidAvatar(
    @StringRes val avatarName: Int,
    @DrawableRes val image: Int
) {
    AVATAR_00(R.string.avatar_name_00, R.drawable.avatar_00),
    AVATAR_01(R.string.avatar_name_01, R.drawable.avatar_11),
    AVATAR_02(R.string.avatar_name_02, R.drawable.avatar_10),
    AVATAR_03(R.string.avatar_name_03, R.drawable.avatar_09),
    AVATAR_04(R.string.avatar_name_04, R.drawable.avatar_08),
    AVATAR_05(R.string.avatar_name_05, R.drawable.avatar_07),
    AVATAR_06(R.string.avatar_name_06, R.drawable.avatar_06),
    AVATAR_07(R.string.avatar_name_07, R.drawable.avatar_05),
    AVATAR_08(R.string.avatar_name_08, R.drawable.avatar_04),
    AVATAR_09(R.string.avatar_name_09, R.drawable.avatar_03),
    AVATAR_10(R.string.avatar_name_10, R.drawable.avatar_02),
    AVATAR_11(R.string.avatar_name_11, R.drawable.avatar_01);
}