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
    UseCase<CreateAgendaItemsUseCase.Params, CreateAgendaItemsUseCase.Result> {

    override fun execute(parameters: Params): Result {

        val items = mutableListOf<AgendaItem>()

        var currentDate = parameters.date

        val scheduledQuests = parameters.scheduledQuests

        while (items.size < parameters.itemsAfter) {

            if (currentDate.dayOfMonth == 1) {
                items.add(AgendaItem.Month(YearMonth.of(currentDate.year, currentDate.monthValue)))
            }

            if (items.size >= parameters.itemsAfter) {
                break
            }

            if (currentDate.dayOfWeek == parameters.firstDayOfWeek) {
                items.add(AgendaItem.Week(currentDate, currentDate.plusDays(6)))
            }

            if (items.size >= parameters.itemsAfter) {
                break
            }

            if (scheduledQuests.containsKey(currentDate)) {
                items.add(AgendaItem.Date(currentDate))

                val dateQuests = scheduledQuests[currentDate]!!
                for (q in dateQuests) {

                    if (items.size >= parameters.itemsAfter) {
                        break
                    }

                    items.add(AgendaItem.QuestItem(q))
                }
            }

            currentDate = currentDate.plusDays(1)
        }

        return Result(items)
    }

    data class Params(
        val date: LocalDate,
        val scheduledQuests: Map<LocalDate, List<Quest>>,
        val itemsAfter: Int,
        val itemsBefore: Int,
        val firstDayOfWeek: DayOfWeek = DateUtils.firstDayOfWeek
    )

    data class Result(val agendaItems: List<AgendaItem>)

    sealed class AgendaItem {
        data class QuestItem(val quest: Quest) : AgendaItem()
        data class Date(val date: LocalDate) : AgendaItem()
        data class Week(val start: LocalDate, val end: LocalDate) : AgendaItem()
        data class Month(val month: YearMonth) : AgendaItem()
    }
}