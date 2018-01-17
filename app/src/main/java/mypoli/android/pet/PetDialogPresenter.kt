package mypoli.android.pet

import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.pet.usecase.FindPetUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/7/17.
 */

data class PetDialogViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null
) : ViewState {
    enum class Type {
        LOADING,
        PET_LOADED
    }
}

object LoadPetIntent : Intent

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