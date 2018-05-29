package io.ipoli.android.dailychallenge.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.dailychallenge.data.persistence.DailyChallengeRepository
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

open class CheckForDailyChallengeCompletionUseCase(
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val questRepository: QuestRepository
) :
    UseCase<Unit, CheckForDailyChallengeCompletionUseCase.Result> {

    override fun execute(parameters: Unit): Result {
        val dc = dailyChallengeRepository.findById(LocalDate.now().startOfDayUTC().toString())
            ?: return Result.NotScheduledForToday

        if (dc.isCompleted) {
            return Result.AlreadyComplete
        }

        val qs = questRepository.findQuestsForDailyChallenge(dc)

        val allComplete = qs.size == 3 && qs.all { it.isCompleted }
        return if (allComplete) {
            dailyChallengeRepository.save(dc.copy(isCompleted = true))
            Result.Complete
        } else
            Result.NotComplete
    }

    sealed class Result {
        object NotScheduledForToday : Result()
        object AlreadyComplete : Result()
        object NotComplete : Result()
        object Complete : Result()
    }
}