package mypoli.android.repeatingquest.show

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.datetime.Duration
import mypoli.android.common.datetime.Minute
import mypoli.android.common.datetime.minutes
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.quest.Color
import mypoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.ProgressModel.COMPLETE
import mypoli.android.repeatingquest.show.RepeatingQuestViewState.Changed.ProgressModel.INCOMPLETE
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */

sealed class RepeatingQuestAction : Action {
    data class Load(val repeatingQuestId: String) : RepeatingQuestAction()
}

sealed class RepeatingQuestViewState : ViewState {

    object Loading : RepeatingQuestViewState()

    data class Changed(
        val name: String,
        val color: Color,
        val nextScheduledDate: LocalDate,
        val totalDuration: Duration<Minute>,
        val currentStreak: Int,
        val repeat: RepeatType,
        val progress: List<ProgressModel>
    ) : RepeatingQuestViewState() {

        enum class ProgressModel {
            COMPLETE, INCOMPLETE
        }

        sealed class RepeatType {
            object Daily : RepeatType()
            data class Weekly(val frequency: Int) : RepeatType()
            data class Monthly(val frequency: Int) : RepeatType()
            object Yearly : RepeatType()
        }
    }
}

object RepeatingQuestReducer : BaseViewStateReducer<RepeatingQuestViewState>() {

    override val stateKey = key<RepeatingQuestViewState>()

    override fun reduce(
        state: AppState,
        subState: RepeatingQuestViewState,
        action: Action
    ) = when (action) {
        is RepeatingQuestAction.Load -> {
            RepeatingQuestViewState.Changed(
                name = "Hello World",
                color = Color.DEEP_ORANGE,
                nextScheduledDate = LocalDate.now(),
                totalDuration = 180.minutes,
                currentStreak = 10,
                repeat = RepeatingQuestViewState.Changed.RepeatType.Weekly(5),
                progress = listOf(COMPLETE, COMPLETE, COMPLETE, INCOMPLETE, INCOMPLETE)
            )
        }
        else -> subState
    }

    override fun defaultState() = RepeatingQuestViewState.Loading
}