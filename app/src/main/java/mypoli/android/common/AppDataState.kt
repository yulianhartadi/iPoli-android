package mypoli.android.common

import mypoli.android.common.datetime.DateUtils
import mypoli.android.common.datetime.datesUntil
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.player.Player
import mypoli.android.quest.Quest
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/24/2018.
 */

sealed class DataLoadedAction : Action {
    data class PlayerChanged(val player: Player) : DataLoadedAction()
    data class TodayQuestsChanged(val quests: List<Quest>) : DataLoadedAction()
    data class ScheduledQuestsChanged(val quests: List<Quest>) : DataLoadedAction()
}

data class AppDataState(
    val today: LocalDate,
    val player: Player?,
    val todayQuests: List<Quest>,
    val scheduleStart: LocalDate,
    val scheduleEnd: LocalDate,
    val scheduledQuests: Map<LocalDate, List<Quest>>
) : State

object AppDataReducer : AppStateReducer<AppDataState> {

    override fun reduce(state: AppState, action: Action) =
        state.appDataState.let {
            when (action) {

                is DataLoadedAction.PlayerChanged -> {
                    it.copy(
                        player = action.player
                    )
                }

                is DataLoadedAction.TodayQuestsChanged -> {
                    it.copy(
                        todayQuests = action.quests
                    )
                }

                is DataLoadedAction.ScheduledQuestsChanged -> {
                    it.copy(
                        scheduledQuests = toSchedule(
                            it.scheduleStart,
                            it.scheduleEnd,
                            action.quests
                        )
                    )
                }
                else -> it
            }

        }

    private fun toSchedule(
        start: LocalDate,
        end: LocalDate,
        quests: List<Quest>
    ): Map<LocalDate, List<Quest>> {
        val schedule = start.datesUntil(end).map { it to mutableListOf<Quest>() }.toMap()
        quests.forEach {
            schedule[it.scheduledDate]!!.add(it)
        }
        return schedule
    }

    override fun defaultState(): AppDataState {
        return AppDataState(
            today = LocalDate.now(),
            player = null,
            todayQuests = listOf(),
            scheduleStart = LocalDate.now().with(TemporalAdjusters.previous(DateUtils.firstDayOfWeek)),
            scheduleEnd = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DateUtils.lastDayOfWeek)),
            scheduledQuests = mapOf()
        )
    }

}