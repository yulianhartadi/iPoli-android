package io.ipoli.android.store.gem

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.store.purchase.InAppPurchaseManager
import io.ipoli.android.store.usecase.PurchaseGemPackUseCase
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/24/2018.
 */

class GemStoreSideEffectHandler : AppSideEffectHandler() {

    private val purchaseGemPackUseCase by required { purchaseGemPackUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is GemStoreAction.Load ->
                action.purchaseManager.loadAll {
                    dispatch(GemStoreAction.GemPacksLoaded(it))
                }

            is GemStoreAction.BuyGemPack ->
                action.purchaseManager.purchase(
                    action.gemPack.type,
                    object : InAppPurchaseManager.PurchaseListener {
                        override fun onPurchased() {
                            val result =
                                purchaseGemPackUseCase
                                    .execute(PurchaseGemPackUseCase.Params(action.gemPack))
                            dispatch(GemStoreAction.GemPackPurchased(result.hasUnlockedPet))
                        }

                        override fun onError() {
                            dispatch(GemStoreAction.PurchaseFailed)
                        }

                    })
        }
    }

    override fun canHandle(action: Action) = action is GemStoreAction

}