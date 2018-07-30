package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/13/18.
 */
class FindNextDateForChallengeUseCase(private val questRepository: QuestRepository) :
    UseCase<FindNextDateForChallengeUseCase.Params, Challenge> {

    override fun execute(parameters: Params): Challenge {
        val challenge = parameters.challenge
        val currentDate = parameters.currentDate

        val nextScheduled =
            questRepository.findNextScheduledNotCompletedForChallenge(challenge.id, currentDate)
                ?: return challenge

        return challenge.copy(
            nextDate = nextScheduled.scheduledDate!!,
            nextStartTime = nextScheduled.startTime,
            nextDuration = nextScheduled.duration
        )
    }

    data class Params(val challenge: Challenge, val currentDate: LocalDate = LocalDate.now())
}