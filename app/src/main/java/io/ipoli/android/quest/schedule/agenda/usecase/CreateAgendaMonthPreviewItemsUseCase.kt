package io.ipoli.android.quest.schedule.agenda.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Color
import org.threeten.bp.LocalDate

class CreateAgendaMonthPreviewItemsUseCase :
    UseCase<CreateAgendaMonthPreviewItemsUseCase.Params, List<CreateAgendaMonthPreviewItemsUseCase.PreviewItem>> {

    override fun execute(parameters: Params): List<PreviewItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    data class Params(val startDate: LocalDate, val endDate: LocalDate)

    data class PreviewItem(val date: LocalDate) {
        sealed class Indicator {
            data class Quest(val color: Color) : Indicator()
            data class Event(val color: Int) : Indicator()
        }
    }
}