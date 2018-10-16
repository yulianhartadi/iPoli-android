package io.ipoli.android.quest.schedule.summary.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.datesBetween
import io.ipoli.android.common.permission.PermissionChecker
import io.ipoli.android.event.Event
import io.ipoli.android.event.persistence.EventRepository
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import org.threeten.bp.LocalDate

class CreateScheduleSummaryItemsUseCase(
    private val eventRepository: EventRepository,
    private val playerRepository: PlayerRepository,
    private val permissionChecker: PermissionChecker
) :
    UseCase<CreateScheduleSummaryItemsUseCase.Params, List<CreateScheduleSummaryItemsUseCase.Schedule>> {

    override fun execute(parameters: Params): List<Schedule> {

        val startDate = parameters.startDate
        val endDate = parameters.endDate

        val p = parameters.player ?: playerRepository.find()!!

        val quests = parameters.quests

        val eventsByDate = getEventsSortedByDate(p, startDate, endDate)

        val questsByDate = quests.groupBy { it.scheduledDate!! }

        return startDate.datesBetween(endDate).map { d ->
            val dailyQuests = questsByDate[d] ?: emptyList()
            val dailyEvents = eventsByDate[d] ?: emptyList()

            val qs = dailyQuests.map {
                Schedule.Item.Quest(it.name, it.color, it.startTime)
            }
            val es = dailyEvents.map {
                Schedule.Item.Event(it.name, it.color, it.startTime)
            }

            val items = (qs + es).sortedBy { it.startTime?.toMinuteOfDay() }

            Schedule(
                date = d,
                items = items
            )
        }
    }

    private fun getEventsSortedByDate(
        p: Player,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, List<Event>> {

        if (!permissionChecker.canReadCalendar()) {
            return emptyMap()
        }

        val calendarIds = p.preferences.syncCalendars.map { it.id.toInt() }.toSet()

        val allEvents = eventRepository.findScheduledBetween(calendarIds, startDate, endDate)
            .filter { !it.isAllDay }

        val (multiDayEvents, singleDayEvents) = allEvents.partition { it.startDate != it.endDate }

        val events = mutableListOf<Event>()
        multiDayEvents.forEach {
            events.add(
                it.copy(
                    endDate = it.startDate,
                    endTime = Time(Time.MINUTES_IN_A_DAY - 1)
                )
            )
            events.add(
                it.copy(
                    startDate = it.endDate,
                    startTime = Time.atHours(0)
                )
            )
            if (it.startDate.plusDays(1) != it.endDate) {
                it.startDate.plusDays(1).datesBetween(it.endDate.minusDays(1)).forEach { date ->
                    events.add(
                        it.copy(
                            startDate = date,
                            endDate = date,
                            startTime = Time.atHours(0),
                            endTime = Time.of(Time.MINUTES_IN_A_DAY - 1)
                        )
                    )
                }
            }
        }

        return (singleDayEvents + events).groupBy { it.startDate }
    }

    data class Schedule(
        val date: LocalDate,
        val items: List<Item>
    ) {

        sealed class Item {

            abstract val startTime: Time?

            data class Quest(val name: String, val color: Color, override val startTime: Time?) :
                Item()

            data class Event(val name: String, val color: Int, override val startTime: Time?) :
                Item()
        }
    }

    data class Params(
        val quests: List<Quest>,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val player: Player? = null
    )
}