package mypoli.android.store.theme.sideeffect

import mypoli.android.common.AppSideEffectHandler
import mypoli.android.common.AppState
import mypoli.android.common.redux.Action
import mypoli.android.store.theme.ThemeStoreAction
import mypoli.android.store.theme.usecase.BuyThemeUseCase
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/25/18.
 */
data class BuyThemeCompletedAction(val result: BuyThemeUseCase.Result) : Action

class ThemeSideEffectHandler : AppSideEffectHandler() {

    private val buyThemeUseCase by required { buyThemeUseCase }
    private val changeThemeUseCase by required { changeThemeUseCase }

    override fun canHandle(action: Action) =
        action is ThemeStoreAction

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is ThemeStoreAction.Buy -> {
                dispatch(BuyThemeCompletedAction(buyThemeUseCase.execute(action.theme)))
            }

            is ThemeStoreAction.Change -> {
                changeThemeUseCase.execute(action.theme)
            }
        }
    }

}