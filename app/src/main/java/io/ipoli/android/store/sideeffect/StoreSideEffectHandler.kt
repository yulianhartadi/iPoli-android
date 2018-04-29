package io.ipoli.android.store.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.view.ColorPickerAction
import io.ipoli.android.player.usecase.BuyColorPackUseCase
import space.traversal.kapsule.required

class StoreSideEffectHandler : AppSideEffectHandler() {

    private val buyColorPackUseCase by required { buyColorPackUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is ColorPickerAction.BuyColorPack -> {
                val result =
                    buyColorPackUseCase.execute(BuyColorPackUseCase.Params(action.colorPack))
                dispatch(ColorPickerAction.BuyColorPackTransactionComplete(result))
            }
        }
    }

    override fun canHandle(action: Action) = action is ColorPickerAction

}