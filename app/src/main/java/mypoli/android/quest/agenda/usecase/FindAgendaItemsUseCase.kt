package mypoli.android.quest.agenda.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/29/2018.
 */
class FindAgendaItemsUseCase(private val questRepository: QuestRepository) :
    UseCase<FindAgendaItemsUseCase.Params, FindAgendaItemsUseCase.Result> {

    override fun execute(parameters: Params) =
        when (parameters) {
            is Params.Before -> executeBefore(parameters)
            is Params.After -> executeAfter(parameters)
            is Params.All -> executeAll(parameters)
        }

    private fun executeAll(parameters: Params.All): Result.All {
        require(parameters.itemsBefore > 0 && parameters.itemsAfter > 0)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun executeAfter(parameters: Params.After): Result.After {
        require(parameters.itemCount > 0)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun executeBefore(parameters: Params.Before): Result.Before {
        require(parameters.itemCount > 0)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    sealed class Params {
        data class Before(val date: LocalDate, val itemCount: Int) : Params()
        data class After(val date: LocalDate, val itemCount: Int) : Params()
        data class All(val date: LocalDate, val itemsBefore: Int, val itemsAfter: Int) : Params()
    }

//    data class Params(
//        val date: LocalDate,
////        val itemCount: Int,
//        val itemsBefore: Int,
//        val itemsAfter: Int
////        val findBefore: Boolean,
////        val firstDayOfWeek: DayOfWeek = DateUtils.firstDayOfWeek
//    )

//    sealed class AgendaItem {
//        data class QuestItem(val quest: Quest) : AgendaItem()
//        data class Date(val date: LocalDate) : AgendaItem()
//        data class Week(val start: LocalDate, val end: LocalDate) : AgendaItem()
//        data class Month(val month: YearMonth) : AgendaItem()
//    }

    //    data class Result(val agendaItems: List<AgendaItem>)
//    data class Result(val start: LocalDate, val end: LocalDate)

    sealed class Result {
        data class Before(val date: LocalDate) : Result()
        data class After(val date: LocalDate) : Result()
        data class All(val start: LocalDate, val end: LocalDate) : Result()
    }
}