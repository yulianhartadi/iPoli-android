package io.ipoli.android.home

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.home.HomeViewState.StateType.*
import io.ipoli.android.player.ExperienceForLevelGenerator
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */
class HomePresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<HomeViewState>, HomeViewState, HomeIntent>(
    HomeViewState(LOADING),
    coroutineContext
) {

    override fun reduceState(intent: HomeIntent, state: HomeViewState) =
        when (intent) {
            is LoadDataIntent -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        actor.send(PlayerChangedIntent(it))
                    }
                }
                state.copy(type = LOADING)
            }

            is PlayerChangedIntent -> {
                val player = intent.player

                val type = if (state.level != 0 && state.level != player.level) {
                    LEVEL_CHANGED
                } else if (state.type != LOADING) {
                    XP_CHANGED
                } else {
                    DATA_LOADED
                }

                state.copy(
                    type = type,
                    level = player.level,
                    progress = player.experience.toInt(),
                    maxProgress = ExperienceForLevelGenerator.forLevel(player.level + 1).toInt()
                )
            }
        }

}