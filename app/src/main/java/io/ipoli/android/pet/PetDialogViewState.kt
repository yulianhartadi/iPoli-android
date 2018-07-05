package io.ipoli.android.pet

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import space.traversal.kapsule.required

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
                state.dataState.player?.let {
                    subState.copy(
                        type = PetDialogViewState.Type.PET_LOADED,
                        petAvatar = it.pet.avatar
                    )
                } ?: subState.copy(type = PetDialogViewState.Type.LOADING)


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

data class PetDialogViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null
) : BaseViewState() {
    enum class Type {
        LOADING,
        PET_LOADED
    }
}

object PetDialogSideEffectHandler : AppSideEffectHandler() {

    private val playerRepository by required { playerRepository }

    override suspend fun doExecute(action: Action, state: AppState) {
        if (action is LoadPetDialogAction && state.dataState.player == null) {
            dispatch(DataLoadedAction.PlayerChanged(playerRepository.find()!!))
        }
    }

    override fun canHandle(action: Action) = action is LoadPetDialogAction
}