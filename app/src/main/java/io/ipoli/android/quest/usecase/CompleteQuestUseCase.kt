package io.ipoli.android.quest.usecase

import io.ipoli.android.challenge.entity.SharingPreference
import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.rate.RatePopupScheduler
import io.ipoli.android.dailychallenge.usecase.CheckForDailyChallengeCompletionUseCase
import io.ipoli.android.friends.feed.persistence.PostRepository
import io.ipoli.android.friends.job.AddPostScheduler
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
    private val reminderScheduler: ReminderScheduler,
    private val rewardScheduler: RewardScheduler,
    private val ratePopupScheduler: RatePopupScheduler,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val checkForDailyChallengeCompletionUseCase: CheckForDailyChallengeCompletionUseCase,
    private val challengeRepository: ChallengeRepository,
    private val addPostScheduler: AddPostScheduler,
    private val postRepository: PostRepository
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

        addPostIfQuestIsPublic(newQuest)

        ratePopupScheduler.schedule()

        checkForDailyChallengeCompletionUseCase.execute(
            CheckForDailyChallengeCompletionUseCase.Params(r.player)
        )

        return newQuest
    }

    private fun addPostIfQuestIsPublic(quest: Quest) {
        if (quest.isFromChallenge) {
            if (challengeRepository.findById(quest.challengeId!!)!!.sharingPreference == SharingPreference.FRIENDS) {
                val hasPostForQuest = try {
                    postRepository.hasPostForQuest(quest.id)
                } catch (e: Exception) {
                    true
                }
                if (!hasPostForQuest) {
                    addPostScheduler.scheduleForQuest(quest.id, quest.challengeId)
                }
            }
        }
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