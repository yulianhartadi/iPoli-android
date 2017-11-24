package io.ipoli.android.pet

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/24/17.
 */
class PetPresenter(coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetViewState>, PetViewState, PetIntent>(
    PetViewState(PetViewState.StateType.LOADING),
    coroutineContext
) {
    override fun reduceState(intent: PetIntent, state: PetViewState): PetViewState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}