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
        parameters.startDate.datesBetween(parameters.endDate).map {

        }
        return Result(listOf(), listOf())
    }

    data class WeekPreviewItem(
        val color: Color,
        val duration: Int,
        val startMinute: Int
    )

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