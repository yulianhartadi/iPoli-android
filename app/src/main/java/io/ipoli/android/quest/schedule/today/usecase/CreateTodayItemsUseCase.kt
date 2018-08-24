package io.ipoli.android.quest.schedule.today.usecase

import io.ipoli.android.Constants
import io.ipoli.android.common.UseCase
import io.ipoli.android.event.Event
import io.ipoli.android.quest.Quest

class CreateTodayItemsUseCase :
    UseCase<CreateTodayItemsUseCase.Params, CreateTodayItemsUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val quests = parameters.quests

        val (complete, incomplete) = quests.partition { it.isCompleted }

        val (scheduled, unscheduled) = incomplete.partition { it.isScheduled }

        val (morning, other) = scheduled.partition { it.startTime!! < Constants.AFTERNOON_TIME_START }
        val (afternoon, evening) = other.partition { it.startTime!! < Constants.EVENING_TIME_START }

        val events = parameters.events

        val (morningEvents, otherEvents) = events.partition { it.startTime < Constants.AFTERNOON_TIME_START }
        val (afternoonEvents, eveningEvents) = otherEvents.partition { it.startTime < Constants.EVENING_TIME_START }

        return Result(
            incomplete = createSectionWithQuests(
                TodayItem.UnscheduledSection,
                unscheduled,
                emptyList()
            ) +
                createSectionWithQuests(TodayItem.MorningSection, morning, morningEvents) +
                createSectionWithQuests(TodayItem.AfternoonSection, afternoon, afternoonEvents) +
                createSectionWithQuests(TodayItem.EveningSection, evening, eveningEvents)
            , complete = complete
        )
    }

    private fun createSectionWithQuests(
        sectionItem: TodayItem,
        quests: List<Quest>,
        events: List<Event>
    ): List<TodayItem> {
        if (quests.isEmpty() && events.isEmpty()) {
            return emptyList()
        }
        val items = mutableListOf<TodayItem>()
        items.addAll(quests.map { TodayItem.QuestItem(it) })
        items.addAll(events.map { TodayItem.EventItem(it) })

        items.sortBy {
            when (it) {
                is TodayItem.QuestItem ->
                    it.quest.startTime?.toMinuteOfDay()
                is TodayItem.EventItem ->
                    it.event.startTime.toMinuteOfDay()
                else -> throw IllegalArgumentException("Can't sort today item $it")
            }
        }

        items.add(0, sectionItem)
        return items
    }

    data class Params(val quests: List<Quest>, val events: List<Event>)

    data class Result(val incomplete: List<TodayItem>, val complete: List<Quest>)

    sealed class TodayItem {

        object UnscheduledSection : TodayItem()
        object MorningSection : TodayItem()
        object AfternoonSection : TodayItem()
        object EveningSection : TodayItem()

        data class QuestItem(val quest: Quest) : TodayItem()
        data class EventItem(val event: Event) : TodayItem()

    }
}