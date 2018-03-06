package mypoli.android.challenge.show

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.datetime.datesBetween
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.quest.Color
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/05/2018.
 */

sealed class ChallengeAction : Action {
    data class Load(val challengeId: String) : ChallengeAction()
}

object ChallengeReducer : BaseViewStateReducer<ChallengeViewState>() {
    override fun reduce(
        state: AppState,
        subState: ChallengeViewState,
        action: Action
    ): ChallengeViewState {
        return when (action) {
            is ChallengeAction.Load -> {
                ChallengeViewState.Changed(
                    id = action.challengeId,
                    name = "Welcome to the World",
                    color = Color.GREEN,
                    completedCount = 18,
                    totalCount = 35,
                    progressPercent = ((18.0 / 35) * 100).toInt(),
                    xAxisLabelCount = 5,
                    chartData = LocalDate.now().minusDays(30).datesBetween(LocalDate.now()).map {
                        it to 5
                    }.toMap().toSortedMap()
                )
            }
            else -> subState
        }
    }

    override fun defaultState() = ChallengeViewState.Loading("")

    override val stateKey = key<ChallengeViewState>()
}

sealed class ChallengeViewState(open val id: String) : ViewState {

    data class Loading(override val id: String) : ChallengeViewState(id)

    data class Changed(
        override val id: String,
        val name: String,
        val color: Color,
        val completedCount: Int,
        val totalCount: Int,
        val progressPercent: Int,
        val xAxisLabelCount: Int,
        val chartData: SortedMap<LocalDate, Int>
    ) : ChallengeViewState(id)
}