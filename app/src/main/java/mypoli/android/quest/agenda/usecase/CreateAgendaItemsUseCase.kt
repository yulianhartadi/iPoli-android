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

            if (items.size >= itemsToFill) {
                break
            }

            if (currentDate.dayOfWeek == firstDayOfWeek) {
                items.add(AgendaItem.Week(currentDate, currentDate.plusDays(6)))
            }

            if (items.size >= itemsToFill) {
                break
            }

            if (scheduledQuests.containsKey(currentDate)) {
                items.add(AgendaItem.Date(currentDate))

                val dateQuests = scheduledQuests[currentDate]!!
                for (q in dateQuests) {

                    if (items.size >= itemsToFill) {
                        break
                    }

                    items.add(AgendaItem.QuestItem(q))
                }
            }

            currentDate = nextDate(currentDate)
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