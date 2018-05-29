package io.ipoli.android.dailychallenge.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.dailychallenge.data.DailyChallenge
import io.ipoli.android.dailychallenge.data.persistence.DailyChallengeRepository
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/29/18.
 */
class LoadDailyChallengeUseCase(
    private val dailyChallengeRepository: DailyChallengeRepository
) : UseCase<LoadDailyChallengeUseCase.Params, DailyChallenge> {

    override fun execute(parameters: Params): DailyChallenge {
        val id = parameters.date.startOfDayUTC().toString()
        val dc =
            dailyChallengeRepository.findById(id)
        if (dc != null) {
            return dc
        }

        return dailyChallengeRepository.save(DailyChallenge())
    }

    data class Params(val date: LocalDate = LocalDate.now())
}