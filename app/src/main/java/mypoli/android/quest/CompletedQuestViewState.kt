package mypoli.android.quest

import mypoli.android.common.datetime.Duration
import mypoli.android.common.datetime.Minute
import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.pet.Food
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/18.
 */
sealed class CompletedQuestIntent : Intent {
    data class LoadData(val questId: String) : CompletedQuestIntent()
}

data class CompletedQuestViewState(
    val type: StateType,
    val name: String? = null,
    val icon: AndroidIcon? = null,
    val color: AndroidColor? = null,
    val totalDuration: Duration<Minute>? = null,
    val completeAt: LocalDate? = null,
    val startedAt: Time? = null,
    val finishedAt: Time? = null,
    val timer: Timer? = null,
    val experience: Int? = null,
    val coins: Int? = null,
    val bounty: Food? = null
) : ViewState {

    enum class StateType {
        LOADING,
        DATA_LOADED
    }

    sealed class Timer {
        data class Pomodoro(
            val completedPomodoros: Int,
            val totalPomodoros: Int,
            val workDuration: Duration<Minute>,
            val overdueWorkDuration: Duration<Minute>,
            val breakDuration: Duration<Minute>,
            val overdueBreakDuration: Duration<Minute>
        ) : Timer()

        data class Countdown(
            val overdueDuration: Duration<Minute>
        ) : Timer()

        object Untracked : Timer()
    }
}