package io.ipoli.android.quest.schedule.summary.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.daysBetween
import io.ipoli.android.common.redux.Action
import io.ipoli.android.event.usecase.FindEventsBetweenDatesUseCase
import io.ipoli.android.player.data.Player
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.schedule.summary.ScheduleSummaryAction
import io.ipoli.android.quest.schedule.summary.ScheduleSummaryViewState
import io.ipoli.android.quest.schedule.summary.usecase.CreateScheduleSummaryUseCase
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.repeatingquest.usecase.CreatePlaceholderQuestsForRepeatingQuestsUseCase
import kotlinx.coroutines.experimental.channels.Channel
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import space.traversal.kapsule.required

object ScheduleSummarySideEffectHandler : AppSideEffectHandler() {

    private val createScheduleSummaryUseCase by required { createScheduleSummaryUseCase }
    private val questRepository by required { questRepository }
    private val createPlaceholderQuestsForRepeatingQuestsUseCase by required { createPlaceholderQuestsForRepeatingQuestsUseCase }
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    private val findEventsBetweenDatesUseCase by required { findEventsBetweenDatesUseCase }

    private var selectedDayQuestsChannel: Channel<List<Quest>>? = null
    private var scheduleSummaryQuestsChannel: Channel<List<Quest>>? = null

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is ScheduleSummaryAction.Load -> {
                startListenForCalendarQuests(action.currentDate)
                listenForScheduleSummary(action.currentDate, state.dataState.player)
            }

            is ScheduleSummaryAction.ChangeDate -> {
                val prevDate = state.stateFor(ScheduleSummaryViewState::class.java).previousDate
                if (prevDate != action.currentDate) {
                    startListenForCalendarQuests(action.currentDate)
                }
                if (prevDate.month != action.currentDate.month) {
                    listenForScheduleSummary(action.currentDate, state.dataState.player)
                }
            }
        }
    }

    private fun listenForScheduleSummary(
        currentDate: LocalDate,
        player: Player?
    ) {
        val startDate = currentDate.with(TemporalAdjusters.firstDayOfMonth())
            .with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

        val lastWeekEndDate = currentDate.with(TemporalAdjusters.lastDayOfMonth())
            .with(TemporalAdjusters.nextOrSame(DateUtils.lastDayOfWeek))

        val daysBetween = startDate.daysBetween(lastWeekEndDate).toInt()

        val endDate = if (daysBetween / 7 == 6) lastWeekEndDate else lastWeekEndDate.plusWeeks(1)

        listenForChanges(
            oldChannel = scheduleSummaryQuestsChannel,
            channelCreator = {
                scheduleSummaryQuestsChannel =
                    questRepository.listenForScheduledBetween(startDate, endDate)
                scheduleSummaryQuestsChannel!!
            },
            onResult = { qs ->

                val placeholderQuests =
                    createPlaceholderQuestsForRepeatingQuestsUseCase.execute(
                        CreatePlaceholderQuestsForRepeatingQuestsUseCase.Params(
                            startDate = startDate,
                            endDate = endDate
                        )
                    )

                val items = createScheduleSummaryUseCase.execute(
                    CreateScheduleSummaryUseCase.Params(
                        quests = qs + placeholderQuests,
                        startDate = startDate,
                        endDate = endDate,
                        player = player
                    )
                )

                dispatch(
                    DataLoadedAction.ScheduleSummaryChanged(
                        currentDate,
                        items
                    )
                )
            }
        )
    }

    private fun startListenForCalendarQuests(
        currentDate: LocalDate
    ) {
        listenForChanges(
            oldChannel = selectedDayQuestsChannel,
            channelCreator = {
                selectedDayQuestsChannel = questRepository.listenForScheduledAt(currentDate)
                selectedDayQuestsChannel!!
            },
            onResult = { qs ->
                val placeholderQuests =
                    createPlaceholderQuestsForRepeatingQuestsUseCase.execute(
                        CreatePlaceholderQuestsForRepeatingQuestsUseCase.Params(
                            startDate = currentDate,
                            endDate = currentDate
                        )
                    )

                val events = findEventsBetweenDatesUseCase.execute(
                    FindEventsBetweenDatesUseCase.Params(
                        startDate = currentDate,
                        endDate = currentDate
                    )
                )

                val schedule =
                    loadScheduleForDateUseCase.execute(
                        LoadScheduleForDateUseCase.Params(
                            startDate = currentDate,
                            endDate = currentDate,
                            quests = qs + placeholderQuests,
                            events = events
                        )
                    )
                dispatch(DataLoadedAction.MonthPreviewScheduleChanged(schedule))
            })
    }

    override fun canHandle(action: Action) = action is ScheduleSummaryAction
}