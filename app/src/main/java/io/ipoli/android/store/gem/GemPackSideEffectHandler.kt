package io.ipoli.android.store.gem

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.view.CurrencyConverterAction
import io.ipoli.android.player.usecase.ConvertCoinsToGemsUseCase
import io.ipoli.android.store.usecase.PurchaseGemPackUseCase
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/24/2018.
 */

object GemPackSideEffectHandler : AppSideEffectHandler() {

    private val purchaseGemPackUseCase by required { purchaseGemPackUseCase }
    private val convertCoinsToGemsUseCase by required { convertCoinsToGemsUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {

            is CurrencyConverterAction.Convert ->
                dispatch(
                    CurrencyConverterAction.ConvertTransactionComplete(
                        convertCoinsToGemsUseCase.execute(ConvertCoinsToGemsUseCase.Params(action.gems))
                    )
                )

            is GemStoreAction.GemPackBought -> {
                val gsvs = state.stateFor(GemStoreViewState::class.java)
                val gemPack = gsvs.gemPacks.first { it.type == action.gemPackType }
                val result = purchaseGemPackUseCase.execute(PurchaseGemPackUseCase.Params(gemPack))
                dispatch(GemStoreAction.GemPackPurchased(result.hasUnlockedPet))
            }
        }
    }

    override fun canHandle(action: Action) =
        action is GemStoreAction || action is CurrencyConverterAction

}