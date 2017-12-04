package io.ipoli.android.pet.shop

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/4/17.
 */
sealed class PetShopIntent : Intent

object LoadDataIntent : PetShopIntent()

data class PetShopViewState(
    val type: StateType = StateType.DATA_LOADED
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED
    }
}