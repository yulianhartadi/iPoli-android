package io.ipoli.android.player.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.common.datetime.minutes
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

class FindAverageFocusedDurationForPeriodUseCase(private val questRepository: QuestRepository) :
    UseCase<FindAverageFocusedDurationForPeriodUseCase.Params, Duration<Minute>> {

    override fun execute(parameters: Params): Duration<Minute> {

        val qs = questRepository.findCompletedInPeriod(
            parameters.currentDate.minusDays(parameters.dayPeriod.toLong() - 1),
            parameters.currentDate
        )

        val productiveMinutes = qs
            .filter { it.hasTimer }
            .sumBy { it.actualDuration.asMinutes.intValue }

        return (productiveMinutes / parameters.dayPeriod).minutes
    }

    data class Params(val dayPeriod: Int = 7, val currentDate: LocalDate = LocalDate.now())
}