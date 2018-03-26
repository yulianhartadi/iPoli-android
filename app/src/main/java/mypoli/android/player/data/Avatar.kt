package mypoli.android.player.data

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import mypoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 8/21/17.
 */
enum class Avatar(val gemPrice: Int) {
    IPOLI_CLASSIC(0),
    IPOLI_YELLOW_GLASSES(0),
    MACARENA(8),
    MACARENA_MEXICAN(8),
    BLONDY(8),
    GREEN_EYES(8),
    PIPILOTA(8),
    OLD_PIRATE(8),
    BEARDY(8),
    DWIGHT(8),
    MICHAEL(8),
    TOBBY(8);
}

enum class AndroidAvatar(
    @StringRes val avatarName: Int,
    @DrawableRes val image: Int
) {
    IPOLI_CLASSIC(R.string.avatar_name_ipoli_classic, R.drawable.avatar_12),
    IPOLI_YELLOW_GLASSES(R.string.avatar_name_ipoli_yellow_glasses, R.drawable.avatar_11),
    MACARENA(R.string.avatar_name_macarena, R.drawable.avatar_10),
    MACARENA_MEXICAN(R.string.avatar_name_macarena_mexican, R.drawable.avatar_09),
    BLONDY(R.string.avatar_name_blondy, R.drawable.avatar_08),
    GREEN_EYES(R.string.avatar_name_green_eyes, R.drawable.avatar_07),
    PIPILOTA(R.string.avatar_name_pipilota, R.drawable.avatar_06),
    OLD_PIRATE(R.string.avatar_name_old_pirate, R.drawable.avatar_05),
    BEARDY(R.string.avatar_beardy, R.drawable.avatar_04),
    DWIGHT(R.string.avatar_name_dwight, R.drawable.avatar_03),
    MICHAEL(R.string.avatar_name_michael, R.drawable.avatar_02),
    TOBBY(R.string.avatar_name_tobby, R.drawable.avatar_01);
}