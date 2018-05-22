package io.ipoli.android.pet

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.redux.Action
import io.ipoli.android.pet.usecase.FindPetUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/7/17.
 */


object LoadPetDialogAction : Action

object PetDialogReducer : BaseViewStateReducer<PetDialogViewState>() {
    override fun reduce(
        state: AppState,
        subState: PetDialogViewState,
        action: Action
    ) =
        when (action) {

            LoadPetDialogAction ->
                subState.copy(
                    type = PetDialogViewState.Type.PET_LOADED,
                    petAvatar = state.dataState.player!!.pet.avatar
                )
            is DataLoadedAction.PlayerChanged ->
                subState.copy(
                    type = PetDialogViewState.Type.PET_LOADED,
                    petAvatar = action.player.pet.avatar
                )

            else -> subState
        }

    override fun defaultState() = PetDialogViewState(PetDialogViewState.Type.LOADING)

    override val stateKey = key<PetDialogViewState>()

}

object LoadPetIntent : Intent

data class PetDialogViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null
) : BaseViewState() {
    enum class Type {
        LOADING,
        PET_LOADED
    }
}