package mypoli.android.quest.schedule.agenda.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.DateUtils
import mypoli.android.quest.Quest
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/29/2018.
 */
class CreateAgendaItemsUseCase :
    UseCase<CreateAgendaItemsUseCase.Params, List<CreateAgendaItemsUseCase.AgendaItem>> {

    override fun execute(parameters: Params): List<CreateAgendaItemsUseCase.AgendaItem> {

        val scheduledQuests = parameters.scheduledQuests.groupBy { it.scheduledDate }

        val beforeItems =
            createItems(
                parameters.date.minusDays(1),
                scheduledQuests,
                parameters.itemsBefore,
                { it.minusDays(1) },
                parameters.firstDayOfWeek
            )
        val afterItems =
            createItems(
                parameters.date,
                scheduledQuests,
                parameters.itemsAfter,
                { it.plusDays(1) },
                parameters.firstDayOfWeek
            )
        return beforeItems + afterItems
    }

    private fun createItems(
        startDate: LocalDate,
        scheduledQuests: Map<LocalDate, List<Quest>>,
        itemsToFill: Int,
        nextDate: (LocalDate) -> LocalDate,
        firstDayOfWeek: DayOfWeek
    ): MutableList<AgendaItem> {
        val items = mutableListOf<AgendaItem>()
        var currentDate = startDate
        while (items.size < itemsToFill) {

            val dateItems = createItemsForDate(currentDate, firstDayOfWeek, scheduledQuests)

            val newDate = nextDate(currentDate)

            if (newDate > currentDate) items.addAll(dateItems)
            else items.addAll(0, dateItems)

            currentDate = newDate
        }

        return items
    }

    private fun createItemsForDate(
        currentDate: LocalDate,
        firstDayOfWeek: DayOfWeek,
        scheduledQuests: Map<LocalDate, List<Quest>>
    ): MutableList<AgendaItem> {
        val dateItems = mutableListOf<AgendaItem>()
        createWeekItem(currentDate, firstDayOfWeek, dateItems)
        createMonthItem(currentDate, dateItems)
        createDateAndQuestItems(scheduledQuests, currentDate, dateItems)
        return dateItems
    }

    private fun createDateAndQuestItems(
        scheduledQuests: Map<LocalDate, List<Quest>>,
        currentDate: LocalDate,
        dateItems: MutableList<AgendaItem>
    ) {
        if (scheduledQuests.containsKey(currentDate)) {
            dateItems.add(AgendaItem.Date(currentDate))
            scheduledQuests[currentDate]!!.forEach { q -> dateItems.add(AgendaItem.QuestItem(q)) }
        }
    }

    private fun createMonthItem(
        currentDate: LocalDate,
        dateItems: MutableList<AgendaItem>
    ) {
        if (currentDate.dayOfMonth == 1) {
            dateItems.add(
                AgendaItem.Month(
                    YearMonth.of(
                        currentDate.year,
                        currentDate.monthValue
                    )
                )
            )
        }
    }

    private fun createWeekItem(
        currentDate: LocalDate,
        firstDayOfWeek: DayOfWeek,
        dateItems: MutableList<AgendaItem>
    ) {
        if (currentDate.dayOfWeek == firstDayOfWeek) {
            dateItems.add(AgendaItem.Week(currentDate, currentDate.plusDays(6)))
        }
    }

    data class Params(
        val date: LocalDate,
        val scheduledQuests: List<Quest>,
        val itemsBefore: Int,
        val itemsAfter: Int,
        val firstDayOfWeek: DayOfWeek = DateUtils.firstDayOfWeek
    )

    sealed class AgendaItem {

        data class QuestItem(val quest: Quest) : AgendaItem() {
            override fun startDate() = quest.scheduledDate
        }

        data class Date(val date: LocalDate) : AgendaItem() {
            override fun startDate() = date
        }

        data class Week(val start: LocalDate, val end: LocalDate) : AgendaItem() {
            override fun startDate() = start
        }

        data class Month(val month: YearMonth) : AgendaItem() {
            override fun startDate() = LocalDate.of(month.year, month.month, 1)
        }


        abstract fun startDate(): LocalDate
    }
}