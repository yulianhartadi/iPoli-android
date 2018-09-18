package io.ipoli.android.quest.usecase

import io.ipoli.android.challenge.entity.SharingPreference
import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.rate.RatePopupScheduler
import io.ipoli.android.dailychallenge.usecase.CheckForDailyChallengeCompletionUseCase
import io.ipoli.android.friends.usecase.SavePostsUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.ReminderScheduler
import io.ipoli.android.quest.job.RewardScheduler
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/27/17.
 */
open class CompleteQuestUseCase(
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository,
    private val challengeRepository: ChallengeRepository,
    private val reminderScheduler: ReminderScheduler,
    private val rewardScheduler: RewardScheduler,
    private val ratePopupScheduler: RatePopupScheduler,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val checkForDailyChallengeCompletionUseCase: CheckForDailyChallengeCompletionUseCase,
    private val savePostsUseCase: SavePostsUseCase
) : UseCase<CompleteQuestUseCase.Params, Quest> {
    override fun execute(parameters: Params): Quest {

        val quest = when (parameters) {
            is Params.WithQuest -> parameters.quest
            is Params.WithQuestId -> {
                val questId = parameters.questId
                require(questId.isNotEmpty()) { "questId cannot be empty" }
                questRepository.findById(questId)!!
            }
        }

        val player = playerRepository.find()!!

        val r =
            rewardPlayerUseCase.execute(RewardPlayerUseCase.Params.ForQuest(quest, player))

        val newQuest = quest.copy(
            completedAtDate = parameters.completedDate,
            completedAtTime = parameters.completedTime,
            reward = r.reward
        )

        questRepository.save(newQuest)

        reminderScheduler.schedule()

        rewardScheduler.schedule(
            reward = r.reward,
            type = RewardScheduler.Type.QUEST,
            entityId = quest.id
        )
        ratePopupScheduler.schedule()

        checkForDailyChallengeCompletionUseCase.execute(
            CheckForDailyChallengeCompletionUseCase.Params(r.player)
        )

        if (newQuest.isFromChallenge) {
            val challenge = challengeRepository.findById(newQuest.challengeId!!)!!
            if (!challenge.isCompleted && challenge.sharingPreference == SharingPreference.FRIENDS) {
                savePostsUseCase.execute(
                    SavePostsUseCase.Params.QuestFromChallengeComplete(
                        quest = newQuest,
                        challenge = challenge,
                        player = r.player
                    )
                )
            }
        }

        return newQuest
    }

    sealed class Params(open val completedDate: LocalDate, open val completedTime: Time) {
        data class WithQuest(
            val quest: Quest,
            override val completedDate: LocalDate = LocalDate.now(),
            override val completedTime: Time = Time.now()
        ) : Params(completedDate, completedTime)

        data class WithQuestId(
            val questId: String,
            override val completedDate: LocalDate = LocalDate.now(),
            override val completedTime: Time = Time.now()
        ) : Params(completedDate, completedTime)
    }
}