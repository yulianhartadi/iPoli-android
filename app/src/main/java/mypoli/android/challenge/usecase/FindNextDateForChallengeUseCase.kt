package mypoli.android.challenge.usecase

import mypoli.android.challenge.entity.Challenge
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.isAfterOrEqual
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/13/18.
 */
class FindNextDateForChallengeUseCase : UseCase<FindNextDateForChallengeUseCase.Params, Challenge> {

    override fun execute(parameters: Params): Challenge {
        val challenge = parameters.challenge
        val currentDate = parameters.currentDate

        val nextDate = challenge.quests.sortedBy { it.scheduledDate }.firstOrNull {
            it.scheduledDate.isAfterOrEqual(currentDate) && !it.isCompleted
        }?.scheduledDate

        if (nextDate == null) {
            return challenge
        }

        val nextQuest = challenge.quests
            .filter { it.scheduledDate == nextDate }
            .sortedWith(Comparator { q1, q2 ->
                val t1 = q1.startTime?.toMillisOfDay() ?: Long.MAX_VALUE
                val t2 = q2.startTime?.toMillisOfDay() ?: Long.MAX_VALUE
                t1.compareTo(t2)
            }).first()

        return challenge.copy(
            nextDate = nextQuest.scheduledDate,
            nextStartTime = nextQuest.startTime,
            nextDuration = nextQuest.duration
        )
    }

    data class Params(val challenge: Challenge, val currentDate: LocalDate = LocalDate.now())
}