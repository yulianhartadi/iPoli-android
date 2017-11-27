package io.ipoli.android.pet

import io.ipoli.android.Constants
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/24/17.
 */
sealed class PetIntent : Intent

object LoadDataIntent : PetIntent()
object ShowFoodList : PetIntent()
object HideFoodList : PetIntent()
object Feed : PetIntent()

data class PetViewState(
    val type: StateType = StateType.DATA_LOADED,
    val maxHP: Int = Constants.MAX_PET_HP,
    val maxMP: Int = Constants.MAX_PET_MP,
    val hp: Int = 0,
    val mp: Int = 0,
    val coinsBonus: Int = 0,
    val xpBonus: Int = 0,
    val unlockChanceBonus: Int = 0,
    val pet: AndroidPetAvatar? = null
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, FOOD_LIST_SHOWN, FOOD_LIST_HIDDEN, PET_FED
    }
}