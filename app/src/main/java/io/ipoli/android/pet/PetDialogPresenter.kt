package io.ipoli.android.pet

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.redux.Action
import io.ipoli.android.pet.usecase.FindPetUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/7/17.
 */


object LoadPetDialogAction : Action

class PetDialogReducer : BaseViewStateReducer<PetDialogViewState>() {
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
) : ViewState {
    enum class Type {
        LOADING,
        PET_LOADED
    }
}

class PetDialogPresenter(
    private val findPetUseCase: FindPetUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetDialogViewState>, PetDialogViewState, LoadPetIntent>(
    PetDialogViewState(PetDialogViewState.Type.LOADING),
    coroutineContext
) {
    override fun reduceState(intent: LoadPetIntent, state: PetDialogViewState) =
        state.copy(
            type = PetDialogViewState.Type.PET_LOADED,
            petAvatar = findPetUseCase.execute(Unit).avatar
        )
}