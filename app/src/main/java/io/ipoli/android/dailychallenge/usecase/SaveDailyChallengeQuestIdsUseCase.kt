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
class SaveDailyChallengeQuestIdsUseCase(
    private val dailyChallengeRepository: DailyChallengeRepository
) : UseCase<SaveDailyChallengeQuestIdsUseCase.Params, DailyChallenge> {

    override fun execute(parameters: Params): DailyChallenge {
        val dc = dailyChallengeRepository.findById(parameters.date.startOfDayUTC().toString())
        requireNotNull(dc)

        return dailyChallengeRepository.save(
            dc!!.copy(
                questIds = parameters.questIds
            )
        )
    }

    data class Params(val questIds: List<String>, val date: LocalDate = LocalDate.now())
}