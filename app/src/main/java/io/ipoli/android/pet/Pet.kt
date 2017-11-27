package io.ipoli.android.pet

import android.support.annotation.IntegerRes
import android.support.annotation.StringRes
import io.ipoli.android.R
import io.ipoli.android.quest.Entity

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/27/17.
 */
data class Pet(
    override val id: String = "",
    val name: String
) : Entity


enum class PetAvatar(
    val code: Int,
    val price: Int,
    @StringRes val petName: Int,
    @IntegerRes val picture: Int,
    @IntegerRes val headPicture: Int) {

    SEAL(1, 600, R.string.pet_seal, R.drawable.pet_1, R.drawable.pet_1_head),
    DONKEY(2, 500, R.string.pet_donkey, R.drawable.pet_2, R.drawable.pet_2_head),
    ELEPHANT(3, 500, R.string.pet_elephant, R.drawable.pet_3, R.drawable.pet_3_head),
    BEAVER(4, 500, R.string.pet_beaver, R.drawable.pet_4, R.drawable.pet_4_head),
    CHICKEN(5, 700, R.string.pet_chicken, R.drawable.pet_5, R.drawable.pet_5_head),
//    BEAR(6, 500, );
}