package io.ipoli.android.quest.schedule.summary.usecase

import io.ipoli.android.Constants
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.*
import io.ipoli.android.event.Event
import io.ipoli.android.event.persistence.EventRepository
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import org.threeten.bp.LocalDate

class CreateScheduleSummaryUseCase(
    private val eventRepository: EventRepository,
    private val playerRepository: PlayerRepository
) :
    UseCase<CreateScheduleSummaryUseCase.Params, List<CreateScheduleSummaryUseCase.ScheduleSummaryItem>> {

    override fun execute(parameters: Params): List<ScheduleSummaryItem> {

        val startDate = parameters.startDate
        val endDate = parameters.endDate

        val p = parameters.player ?: playerRepository.find()!!

        val quests = parameters.quests

        val calendarIds = p.preferences.syncCalendars.map { it.id.toInt() }.toSet()
        val allEvents = eventRepository.findScheduledBetween(calendarIds, startDate, endDate)

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

        val questsByDate = quests.groupBy { it.scheduledDate!! }
        val eventsByDate = (singleDayEvents + events).groupBy { it.startDate }

        return startDate.datesBetween(endDate).map {
            val dailyQuests = questsByDate[it] ?: emptyList()
            val dailyEvents = eventsByDate[it] ?: emptyList()

            val tagColors =
                dailyQuests
                    .filter { it.tags.isNotEmpty() }
                    .groupBy { it.tags.first().color }
                    .map { Pair(it.key, it.value.sumBy { it.duration }) }
                    .sortedByDescending { it.second }
                    .map { it.first }

            val scheduledQuests = dailyQuests.filter { it.isScheduled }

            ScheduleSummaryItem(
                it,
                createMorningFullness(scheduledQuests, dailyEvents),
                createAfternoonFullness(scheduledQuests, dailyEvents),
                createEveningFullness(scheduledQuests, dailyEvents),
                tagColors
            )
        }
    }

    private fun createMorningFullness(
        scheduledQuests: List<Quest>,
        dailyEvents: List<Event>
    ) =
        createFullness(
            scheduledQuests = scheduledQuests,
            dailyEvents = dailyEvents,
            timeOfDayStart = Constants.MORNING_TIME_START.toMinuteOfDay(),
            timeOfDayEnd = Constants.AFTERNOON_TIME_START.toMinuteOfDay() - 1,
            intervalDuration = MORNING_DURATION
        )

    private fun createAfternoonFullness(
        scheduledQuests: List<Quest>,
        dailyEvents: List<Event>
    ) =
        createFullness(
            scheduledQuests = scheduledQuests,
            dailyEvents = dailyEvents,
            timeOfDayStart = Constants.AFTERNOON_TIME_START.toMinuteOfDay(),
            timeOfDayEnd = Constants.EVENING_TIME_START.toMinuteOfDay() - 1,
            intervalDuration = AFTERNOON_DURATION
        )

    private fun createEveningFullness(
        scheduledQuests: List<Quest>,
        dailyEvents: List<Event>
    ) =
        createFullness(
            scheduledQuests = scheduledQuests,
            dailyEvents = dailyEvents,
            timeOfDayStart = Constants.EVENING_TIME_START.toMinuteOfDay(),
            timeOfDayEnd = Time.at(23, 59).toMinuteOfDay(),
            intervalDuration = EVENING_DURATION
        )

    private fun createFullness(
        scheduledQuests: List<Quest>,
        dailyEvents: List<Event>,
        timeOfDayStart: Int,
        timeOfDayEnd: Int,
        intervalDuration: Duration<Minute>
    ): ScheduleSummaryItem.Fullness {
        var totalDuration = 0

        scheduledQuests.forEach {
            val start2 = it.startTime!!.toMinuteOfDay()
            val end2 = it.endTime!!.toMinuteOfDay()

            if (doOverlap(timeOfDayStart, timeOfDayEnd, start2, end2)) {
                totalDuration += overlapDuration(timeOfDayStart, timeOfDayEnd, start2, end2)
            }
        }

        dailyEvents.forEach {
            val start2 = it.startTime.toMinuteOfDay()
            val end2 = it.endTime.toMinuteOfDay()

            if (doOverlap(timeOfDayStart, timeOfDayEnd, start2, end2)) {
                totalDuration += overlapDuration(timeOfDayStart, timeOfDayEnd, start2, end2)
            }
        }

        val percentage = totalDuration / intervalDuration.intValue.toDouble()

        return when {
            percentage < 0.001 -> ScheduleSummaryItem.Fullness.NONE
            percentage < FULLNESS_LIGHT_THRESHOLD -> ScheduleSummaryItem.Fullness.LIGHT
            percentage < FULLNESS_MEDIUM_THRESHOLD -> ScheduleSummaryItem.Fullness.MEDIUM
            else -> ScheduleSummaryItem.Fullness.HIGH
        }
    }

    //a1<=b2 && a2<=b1  (a1b1 a2b2)
    private fun doOverlap(start1: Int, end1: Int, start2: Int, end2: Int) =
        start1 <= end2 && start2 <= end1

    //max(a1, a2) min(b1, b2)
    private fun overlapDuration(start1: Int, end1: Int, start2: Int, end2: Int) =
        Math.min(end1, end2) - Math.max(start1, start2) + 1


    data class ScheduleSummaryItem(
        val date: LocalDate,
        val morningFullness: Fullness,
        val afternoonFullness: Fullness,
        val eveningFullness: Fullness,
        val tagColors: List<Color>
    ) {

        enum class Fullness {
            NONE, LIGHT, MEDIUM, HIGH
        }
    }

    data class Params(
        val quests: List<Quest>,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val player: Player? = null
    )

    companion object {
        const val FULLNESS_LIGHT_THRESHOLD = 0.35
        const val FULLNESS_MEDIUM_THRESHOLD = 0.65

        val MORNING_DURATION =
            (Constants.AFTERNOON_TIME_START - Constants.MORNING_TIME_START).toMinuteOfDay().minutes
        val AFTERNOON_DURATION =
            (Constants.EVENING_TIME_START - Constants.AFTERNOON_TIME_START).toMinuteOfDay().minutes
        val EVENING_DURATION =
            ((Time.at(23, 59) - Constants.EVENING_TIME_START).toMinuteOfDay() + 1).minutes
    }
}