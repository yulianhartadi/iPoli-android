package io.ipoli.android.pet

import android.support.annotation.IntegerRes
import android.support.annotation.StringRes
import io.ipoli.android.Constants
import io.ipoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/27/17.
 */
data class Pet(
    val name: String,
    val avatar: PetAvatar,
    val moodPoints: Int = Constants.DEFAULT_PET_HP,
    val healthPoints: Int = Constants.DEFAULT_PET_HP,
    val experienceBonus: Int = moodPoints / 20,
    val coinBonus: Int = moodPoints / 20,
    val unlockChanceBonus: Int = moodPoints / 20
)

enum class PetAvatar(val price: Int) {
    SEAL(600),
    DONKEY(500),
    ELEPHANT(500),
    BEAVER(500),
    CHICKEN(700),
    BEAR(500),
    LION(500),
    CAT(500),
    MONKEY(500),
    DUCK(500),
    PIG(500),
    ZEBRA(500)
}


enum class AndroidPetAvatar(
    @StringRes val petName: Int,
    @IntegerRes val picture: Int,
    @IntegerRes val headPicture: Int) {

    SEAL(R.string.pet_seal, R.drawable.pet_1, R.drawable.pet_1_head),
    DONKEY(R.string.pet_donkey, R.drawable.pet_2, R.drawable.pet_2_head),
    ELEPHANT(R.string.pet_elephant, R.drawable.pet_3, R.drawable.pet_3_head),
    BEAVER(R.string.pet_beaver, R.drawable.pet_4, R.drawable.pet_4_head),
    CHICKEN(R.string.pet_chicken, R.drawable.pet_5, R.drawable.pet_5_head),
    BEAR(R.string.pet_chicken, R.drawable.pet_6, R.drawable.pet_6_head),
    LION(R.string.pet_chicken, R.drawable.pet_7, R.drawable.pet_7_head),
    CAT(R.string.pet_chicken, R.drawable.pet_8, R.drawable.pet_8_head),
    MONKEY(R.string.pet_chicken, R.drawable.pet_9, R.drawable.pet_9_head),
    DUCK(R.string.pet_chicken, R.drawable.pet_10, R.drawable.pet_10_head),
    PIG(R.string.pet_chicken, R.drawable.pet_11, R.drawable.pet_11_head),
    ZEBRA(R.string.pet_chicken, R.drawable.pet_12, R.drawable.pet_12_head)
}