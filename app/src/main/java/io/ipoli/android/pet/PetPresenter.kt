package io.ipoli.android.pet

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.PetViewState.StateType.*
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/24/17.
 */
class PetPresenter(coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetViewState>, PetViewState, PetIntent>(
    PetViewState(LOADING),
    coroutineContext
) {
    override fun reduceState(intent: PetIntent, state: PetViewState) =
        when (intent) {
            is LoadDataIntent -> {
                state.copy(
                    type = DATA_LOADED
                )
            }
            is ShowFoodList -> {
                state.copy(
                    type = FOOD_LIST_SHOWN
                )
            }

            is HideFoodList -> {
                state.copy(
                    type = FOOD_LIST_HIDDEN
                )
            }

            is Feed -> {
                state.copy(
                    type = PET_FED
                )
            }
        }

}