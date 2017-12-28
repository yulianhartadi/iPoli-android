package mypoli.android.store.avatars.data

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import mypoli.android.R


/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 8/21/17.
 */
enum class Avatar(val code: Int,
                  val price: Int,
                  @param:StringRes val avatarName: Int,
                  @param:DrawableRes val picture: Int) {

    IPOLI_CLASSIC(1, 0, R.string.avatar_name_ipoli_classic, R.drawable.avatar_12),
    IPOLI_YELLOW_GLASSES(2, 0, R.string.avatar_name_ipoli_yellow_glasses, R.drawable.avatar_11),
    MACARENA(3, 500, R.string.avatar_name_macarena, R.drawable.avatar_10),
    MACARENA_MEXICAN(4, 500, R.string.avatar_name_macarena_mexican, R.drawable.avatar_09),
    BLONDY(5, 500, R.string.avatar_name_blondy, R.drawable.avatar_08),
    GREEN_EYES(6, 500, R.string.avatar_name_green_eyes, R.drawable.avatar_07),
    PIPILOTA(7, 500, R.string.avatar_name_pipilota, R.drawable.avatar_06),
    OLD_PIRATE(8, 500, R.string.avatar_name_old_pirate, R.drawable.avatar_05),
    BEARD_GUY(9, 500, R.string.avatar_name_beard_guy, R.drawable.avatar_04),
    DWIGHT(10, 500, R.string.avatar_name_dwight, R.drawable.avatar_03),
    MICHAEL(11, 500, R.string.avatar_name_michael, R.drawable.avatar_02),
    TOBBY(12, 500, R.string.avatar_name_tobby, R.drawable.avatar_01);

    companion object {
        fun fromCode(code: Int): Avatar? =
            values().firstOrNull { it.code == code }

    }
}