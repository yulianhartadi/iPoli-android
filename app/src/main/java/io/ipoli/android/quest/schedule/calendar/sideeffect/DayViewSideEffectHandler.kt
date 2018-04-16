package io.ipoli.android.quest.schedule.calendar.sideeffect

import io.ipoli.android.common.*
import io.ipoli.android.common.async.ChannelRelay
import io.ipoli.android.common.redux.Action
import io.ipoli.android.event.usecase.FindEventsBetweenDatesUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.schedule.calendar.dayview.view.DayViewAction
import io.ipoli.android.quest.schedule.calendar.dayview.view.DayViewState
import io.ipoli.android.quest.timer.usecase.CompleteTimeRangeUseCase
import io.ipoli.android.quest.usecase.CompleteQuestUseCase
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.quest.usecase.Result
import io.ipoli.android.quest.usecase.SaveQuestUseCase
import io.ipoli.android.repeatingquest.usecase.CreatePlaceholderQuestsForRepeatingQuestsUseCase
import org.threeten.bp.LocalDate
import space.traversal.kapsule.required

class DayViewSideEffectHandler : AppSideEffectHandler() {
    private val saveQuestUseCase by required { saveQuestUseCase }
    private val questRepository by required { questRepository }
    private val findEventsBetweenDatesUseCase by required { findEventsBetweenDatesUseCase }
    private val removeQuestUseCase by required { removeQuestUseCase }
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    private val undoRemoveQuestUseCase by required { undoRemoveQuestUseCase }
    private val createPlaceholderQuestsForRepeatingQuestsUseCase by required { createPlaceholderQuestsForRepeatingQuestsUseCase }
    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }
    private val completeQuestUseCase by required { completeQuestUseCase }
    private val undoCompletedQuestUseCase by required { undoCompletedQuestUseCase }

    data class ScheduledQuestsParams(val startDate: LocalDate, val endDate: LocalDate)

    private val scheduledQuestsChannelRelay =
        ChannelRelay<List<Quest>, ScheduledQuestsParams>(
            producer = { c, p ->
                questRepository
                    .listenForScheduledBetween(
                        startDate = p.startDate,
                        endDate = p.endDate,
                        channel = c
                    )
            },
            consumer = { qs, p ->
                val placeholderQuests =
                    createPlaceholderQuestsForRepeatingQuestsUseCase.execute(
                        CreatePlaceholderQuestsForRepeatingQuestsUseCase.Params(
                            startDate = p.startDate,
                            endDate = p.endDate
                        )
                    )

                val events = findEventsBetweenDatesUseCase.execute(
                    FindEventsBetweenDatesUseCase.Params(
                        startDate = p.startDate,
                        endDate = p.endDate
                    )
                )

                val schedule =
                    loadScheduleForDateUseCase.execute(
                        LoadScheduleForDateUseCase.Params(
                            startDate = p.startDate,
                            endDate = p.endDate,
                            quests = qs + placeholderQuests,
                            events = events
                        )
                    )
                dispatch(DataLoadedAction.CalendarScheduleChanged(schedule))
            }
        )

    override suspend fun doExecute(action: Action, state: AppState) {
        val a = (action as? NamespaceAction)?.source ?: action
        when (a) {

            is LoadDataAction.All ->
                startListenForCalendarQuests(state.dataState.today)

            DayViewAction.AddQuest ->
                saveQuest(state, action)

            DayViewAction.EditQuest ->
                saveQuest(state, action)

            DayViewAction.EditUnscheduledQuest ->
                saveQuest(state, action)

            is DayViewAction.RemoveQuest ->
                removeQuestUseCase.execute(a.questId)

            is DayViewAction.UndoRemoveQuest ->
                undoRemoveQuestUseCase.execute(a.questId)

            is DayViewAction.Load ->
                startListenForCalendarQuests(a.currentDate)

            is DayViewAction.CompleteQuest -> {
                val questId = a.questId
                if (a.isStarted) {
                    completeTimeRangeUseCase.execute(
                        CompleteTimeRangeUseCase.Params(
                            questId
                        )
                    )
                } else {
                    completeQuestUseCase.execute(
                        CompleteQuestUseCase.Params.WithQuestId(
                            questId
                        )
                    )
                }
            }

            is DayViewAction.UndoCompleteQuest -> {
                undoCompletedQuestUseCase.execute(a.questId)
            }
        }
    }

    private fun startListenForCalendarQuests(
        currentDate: LocalDate
    ) {
        scheduledQuestsChannelRelay.listen(
            ScheduledQuestsParams(
                startDate = currentDate.minusDays(2),
                endDate = currentDate.plusDays(2)
            )
        )
    }

    private fun saveQuest(
        state: AppState,
        action: Action
    ) {
        val dayViewState: DayViewState = state.stateFor(
            "${(action as NamespaceAction).namespace}/${DayViewState::class.java.simpleName}"
        )

        val scheduledDate = dayViewState.scheduledDate ?: dayViewState.currentDate
        val reminder = if (dayViewState.startTime != null && dayViewState.reminder != null) {
            Reminder.create(
                dayViewState.reminder,
                scheduledDate,
                dayViewState.startTime
            )
        } else if (dayViewState.editId.isEmpty()) {
            Reminder("", dayViewState.startTime!!, scheduledDate)
        } else {
            null
        }

        val questParams = SaveQuestUseCase.Parameters(
            id = dayViewState.editId,
            name = dayViewState.name,
            subQuests = null,
            color = dayViewState.color!!,
            icon = dayViewState.icon,
            scheduledDate = scheduledDate,
            startTime = dayViewState.startTime,
            duration = dayViewState.duration!!,
            reminders = reminder?.let { listOf(it) },
            repeatingQuestId = dayViewState.repeatingQuestId,
            tags = null
        )
        val result = saveQuestUseCase.execute(questParams)

        when (result) {
            is Result.Invalid -> {
                dispatch(
                    DayViewAction.SaveInvalidQuest(
                        result
                    )
                )
            }
            else -> dispatch(DayViewAction.QuestSaved)
        }
    }

    override fun canHandle(action: Action): Boolean {
        val a = (action as? NamespaceAction)?.source ?: action
        return a is DayViewAction || a === LoadDataAction.All
    }
}