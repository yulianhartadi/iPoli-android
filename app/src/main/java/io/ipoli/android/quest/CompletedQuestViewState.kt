package io.ipoli.android.quest

import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.AndroidIcon
import io.ipoli.android.pet.Food
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
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
    val bounty: Food? = null,
    val playerLevel: Int? = null,
    val playerLevelProgress: Int? = null,
    val playerLevelMaxProgress: Int? = null
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
            val duration: Duration<Minute>,
            val overdueDuration: Duration<Minute>
        ) : Timer()

        object Untracked : Timer()
    }
}