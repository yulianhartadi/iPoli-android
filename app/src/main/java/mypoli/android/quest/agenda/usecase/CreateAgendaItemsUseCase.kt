package mypoli.android.quest.agenda.usecase

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
            createBeforeItems(
                parameters.date.minusDays(1),
                scheduledQuests,
                parameters.itemsBefore,
                parameters.firstDayOfWeek
            )
        val afterItems =
            createAfterItems(
                parameters.date,
                scheduledQuests,
                parameters.itemsAfter,
                parameters.firstDayOfWeek
            )
        return beforeItems + afterItems
    }

    private fun createBeforeItems(
        startDate: LocalDate,
        scheduledQuests: Map<LocalDate, List<Quest>>,
        itemsToFill: Int,
        firstDayOfWeek: DayOfWeek
    ): MutableList<AgendaItem> {
        val items = mutableListOf<AgendaItem>()
        var currentDate = startDate
        while (items.size < itemsToFill) {

            if (scheduledQuests.containsKey(currentDate)) {
                scheduledQuests[currentDate]!!.reversed()
                    .forEach { q -> items.add(0, AgendaItem.QuestItem(q)) }
                items.add(0, AgendaItem.Date(currentDate))
            }

            if (items.size >= itemsToFill) break

            if (currentDate.dayOfMonth == 1) {
                items.add(
                    0,
                    AgendaItem.Month(
                        YearMonth.of(
                            currentDate.year,
                            currentDate.monthValue
                        )
                    )
                )
            }

            if (items.size >= itemsToFill) break

            if (currentDate.dayOfWeek == firstDayOfWeek) {
                items.add(0, AgendaItem.Week(currentDate, currentDate.plusDays(6)))
            }

            currentDate = currentDate.minusDays(1)
        }

        return items
    }

    private fun createAfterItems(
        startDate: LocalDate,
        scheduledQuests: Map<LocalDate, List<Quest>>,
        itemsToFill: Int,
        firstDayOfWeek: DayOfWeek
    ): MutableList<AgendaItem> {
        val items = mutableListOf<AgendaItem>()
        var currentDate = startDate
        while (items.size < itemsToFill) {

            if (currentDate.dayOfWeek == firstDayOfWeek) {
                items.add(AgendaItem.Week(currentDate, currentDate.plusDays(6)))
            }

            if (items.size >= itemsToFill) break

            if (currentDate.dayOfMonth == 1) {
                items.add(
                    AgendaItem.Month(
                        YearMonth.of(
                            currentDate.year,
                            currentDate.monthValue
                        )
                    )
                )
            }

            if (items.size >= itemsToFill) break

            if (scheduledQuests.containsKey(currentDate)) {
                items.add(AgendaItem.Date(currentDate))
                scheduledQuests[currentDate]!!.forEach { q -> items.add(AgendaItem.QuestItem(q)) }
            }

            currentDate = currentDate.plusDays(1)
        }

        return items
    }

    data class Params(
        val date: LocalDate,
        val scheduledQuests: List<Quest>,
        val itemsAfter: Int,
        val itemsBefore: Int,
        val firstDayOfWeek: DayOfWeek = DateUtils.firstDayOfWeek
    )

    sealed class AgendaItem {
        data class QuestItem(val quest: Quest) : AgendaItem()
        data class Date(val date: LocalDate) : AgendaItem()
        data class Week(val start: LocalDate, val end: LocalDate) : AgendaItem()
        data class Month(val month: YearMonth) : AgendaItem()
    }
}