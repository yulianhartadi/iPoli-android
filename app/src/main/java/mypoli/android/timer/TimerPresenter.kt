package mypoli.android.timer

import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 6.01.18.
 */
class TimerPresenter(
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase,
    coroutineContext: CoroutineContext) : BaseMviPresenter<ViewStateRenderer<TimerViewState>, TimerViewState, TimerIntent>(
    TimerViewState(TimerViewState.StateType.LOADING),
    coroutineContext
) {
    override fun reduceState(intent: TimerIntent, state: TimerViewState): TimerViewState {
        return state
    }

}