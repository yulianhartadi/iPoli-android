package io.ipoli.android.friends.middleware

import io.ipoli.android.MyPoliApp
import io.ipoli.android.common.AppState
import io.ipoli.android.common.di.BackgroundModule
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.AsyncMiddleware
import io.ipoli.android.common.redux.Dispatcher
import io.ipoli.android.friends.usecase.SavePostsUseCase
import io.ipoli.android.player.view.LevelUpAction
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/19/2018.
 */
object CreatePostsMiddleware : AsyncMiddleware<AppState>, Injects<BackgroundModule> {

    private val savePostsUseCase by required { savePostsUseCase }

    override fun onCreate() {
        inject(MyPoliApp.backgroundModule(MyPoliApp.instance))
    }

    override fun onExecute(state: AppState, dispatcher: Dispatcher, action: Action) {
        val p = state.dataState.player
        when (action) {
            is LevelUpAction.Load -> {
                try {
                    savePostsUseCase.execute(
                        SavePostsUseCase.Params.LevelUp(
                            action.newLevel,
                            player = p
                        )
                    )
                } catch (e: Throwable) {
                }
            }
        }
    }

}