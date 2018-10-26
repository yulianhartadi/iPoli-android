package io.ipoli.android.quest.schedule.agenda.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.datesBetween
import io.ipoli.android.event.Event
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/26/18.
 */
class CreateAgendaPreviewItemsUseCase :
    UseCase<CreateAgendaPreviewItemsUseCase.Params, CreateAgendaPreviewItemsUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val dayQuests = mutableMapOf<LocalDate, List<Quest>>()
        parameters.quests.forEach {
            val date = it.scheduledDate!!
            val list =
                if (!dayQuests.containsKey(date)) listOf()
                else dayQuests[date]!!
            dayQuests[date] = list + it
        }

        val dayEvents = mutableMapOf<LocalDate, List<Event>>()
        parameters.events.forEach {
            val date = it.startDate
            val list =
                if (!dayEvents.containsKey(date)) listOf()
                else dayEvents[date]!!
            dayEvents[date] = list + it
        }

        val weekItems = parameters.startDate.datesBetween(parameters.endDate).map {

            val indicators = mutableListOf<WeekPreviewItem.Indicator>()
            dayQuests[it]?.forEach { q ->
                if(q.startTime != null) {
                    indicators.add(
                        WeekPreviewItem.Indicator.Quest(
                            startMinute = q.startTime!!.toMinuteOfDay(),//add offset
                            duration = q.duration,
                            color = q.color
                        )
                    )
                }
            }

            dayEvents[it]?.forEach { e ->
                indicators.add(
                    WeekPreviewItem.Indicator.Event(
                        startMinute = e.startTime.toMinuteOfDay(),//add offset
                        duration = e.duration.intValue,
                        color = e.color
                    )
                )
            }


            WeekPreviewItem(
                date = it,
                indicators = indicators.sortedWith(
                    compareBy<WeekPreviewItem.Indicator> { i -> i.startMinute }
                        .thenByDescending { i -> i.duration }
                )
            )
        }
        return Result(weekItems = weekItems, monthItems = listOf())
    }

    data class WeekPreviewItem(
        val date: LocalDate,
        val indicators: List<Indicator>
    ) {
        sealed class Indicator() {
            abstract val duration: Int
            abstract val startMinute: Int

            data class Quest(
                val color: Color,
                override val duration: Int,
                override val startMinute: Int
            ) : Indicator()

            data class Event(
                val color: Int,
                override val duration: Int,
                override val startMinute: Int
            ) : Indicator()
        }
    }

    data class MonthPreviewItem(val date: LocalDate) {
        sealed class Indicator {
            data class Quest(val color: Color) : Indicator()
            data class Event(val color: Int) : Indicator()
        }
    }

    data class Params(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val quests: List<Quest>,
        val events: List<Event>
    )

    data class Result(val weekItems: List<WeekPreviewItem>, val monthItems: List<MonthPreviewItem>)
}