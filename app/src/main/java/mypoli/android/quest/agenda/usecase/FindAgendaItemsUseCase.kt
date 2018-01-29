package mypoli.android.quest.agenda.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.DateUtils
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/29/2018.
 */
class FindAgendaItemsUseCase(private val questRepository: QuestRepository) :
    UseCase<FindAgendaItemsUseCase.Params, FindAgendaItemsUseCase.Result> {

    override fun execute(parameters: Params): Result {
        require(parameters.itemCount >= 0)

        if (parameters.itemCount == 0) {
            return Result(listOf())
        }

        val lastScheduledDate =
            questRepository.findLastScheduledDate(parameters.date, parameters.itemCount)

        val lastDayOfWeek = parameters.firstDayOfWeek.plus(6)

        if (lastScheduledDate == null) {
            return Result(
                listOf(
                    AgendaItem.Week(
                        parameters.date.with(parameters.firstDayOfWeek),
                        parameters.date.with(lastDayOfWeek)
                    )
                )
            )
        }

        return Result(listOf())
    }

    data class Params(
        val date: LocalDate,
        val itemCount: Int,
        val findBefore: Boolean,
        val firstDayOfWeek: DayOfWeek = DateUtils.firstDayOfWeek
    )

    sealed class AgendaItem {
        data class QuestItem(val quest: Quest) : AgendaItem()
        data class Date(val date: LocalDate) : AgendaItem()
        data class Week(val start: LocalDate, val end: LocalDate) : AgendaItem()
        data class Month(val month: YearMonth) : AgendaItem()
    }

    data class Result(val agendaItems: List<AgendaItem>)
}