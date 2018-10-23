package io.ipoli.android.dailychallenge.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.dailychallenge.data.persistence.DailyChallengeRepository
import io.ipoli.android.dailychallenge.job.DailyChallengeCompleteScheduler
import io.ipoli.android.friends.usecase.SavePostsUseCase
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

open class CheckForDailyChallengeCompletionUseCase(
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val savePostsUseCase: SavePostsUseCase,
    private val dailyChallengeCompleteScheduler: DailyChallengeCompleteScheduler
) :
    UseCase<CheckForDailyChallengeCompletionUseCase.Params, CheckForDailyChallengeCompletionUseCase.Result> {

    override fun execute(parameters: CheckForDailyChallengeCompletionUseCase.Params): Result {
        val dc = dailyChallengeRepository.findForDate(LocalDate.now())
            ?: return Result.NotScheduledForToday

        if (dc.isCompleted) {
            return Result.AlreadyComplete
        }

        val qs = questRepository.findQuestsForDailyChallenge(dc)

        val allComplete = qs.size == 3 && qs.all { it.isCompleted }
        return if (allComplete) {

            val p = parameters.player ?: playerRepository.find()!!

            val reward =
                rewardPlayerUseCase.execute(RewardPlayerUseCase.Params.ForDailyChallenge(dc, p))
                    .reward
            dailyChallengeRepository.save(dc.copy(isCompleted = true, reward = reward))
            dailyChallengeCompleteScheduler.schedule(reward.experience, reward.coins)
            try {
                savePostsUseCase.execute(SavePostsUseCase.Params.DailyChallengeComplete())
            } catch (e: Throwable) {
            }

            Result.Complete
        } else
            Result.NotComplete
    }

    data class Params(val player: Player? = null)

    sealed class Result {
        object NotScheduledForToday : Result()
        object AlreadyComplete : Result()
        object NotComplete : Result()
        object Complete : Result()
    }
}