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
//        val dateToQuests = parameters.quests.

        val weekItems = parameters.startDate.datesBetween(parameters.endDate).map {
            WeekPreviewItem(
                date = it,
                indicators = listOf(
                    WeekPreviewItem.Indicator.Quest(
                        startMinute = 20,
                        duration = 240,
                        color = Color.GREEN
                    )
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