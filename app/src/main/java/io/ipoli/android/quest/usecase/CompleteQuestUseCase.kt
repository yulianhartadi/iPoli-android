package io.ipoli.android.quest.usecase

import io.ipoli.android.Constants
import io.ipoli.android.common.SimpleReward
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.rate.RatePopupScheduler
import io.ipoli.android.dailychallenge.job.DailyChallengeCompleteScheduler
import io.ipoli.android.dailychallenge.usecase.CheckForDailyChallengeCompletionUseCase
import io.ipoli.android.pet.Food
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.QuestCompleteScheduler
import io.ipoli.android.quest.job.ReminderScheduler
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/27/17.
 */
open class CompleteQuestUseCase(
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository,
    private val reminderScheduler: ReminderScheduler,
    private val questCompleteScheduler: QuestCompleteScheduler,
    private val ratePopupScheduler: RatePopupScheduler,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val checkForDailyChallengeCompletionUseCase: CheckForDailyChallengeCompletionUseCase,
    private val dailyChallengeCompleteScheduler: DailyChallengeCompleteScheduler,
    private val randomSeed: Long? = null
) : UseCase<CompleteQuestUseCase.Params, Quest> {
    override fun execute(parameters: Params): Quest {

        val quest = when (parameters) {
            is Params.WithQuest -> parameters.quest
            is Params.WithQuestId -> {
                val questId = parameters.questId
                require(questId.isNotEmpty(), { "questId cannot be empty" })
                questRepository.findById(questId)!!
            }
        }

        val pet = playerRepository.find()!!.pet


        val experience = quest.experience ?: experience(pet.experienceBonus)
        val coins = quest.coins ?: coins(pet.coinBonus)
        val bounty = quest.bounty ?: bounty(pet.itemDropBonus)
        val newQuest = quest.copy(
            completedAtDate = LocalDate.now(),
            completedAtTime = Time.now(),
            experience = experience,
            coins = coins,
            bounty = bounty
        )

        questRepository.save(newQuest)

        reminderScheduler.schedule()

        val reward = SimpleReward(
            newQuest.experience!!,
            newQuest.coins!!,
            if (quest.bounty == null) bounty else Quest.Bounty.None
        )
        rewardPlayerUseCase.execute(reward)

        questCompleteScheduler.schedule(reward)
        ratePopupScheduler.schedule()

        val r = checkForDailyChallengeCompletionUseCase.execute(Unit)
        if (r == CheckForDailyChallengeCompletionUseCase.Result.Complete) {
            dailyChallengeCompleteScheduler.schedule(
                experience(100f),
                coins(100f)
            )
        }

        return newQuest
    }

    private fun coins(coinBonusPercentage: Float): Int {
        val rewards = intArrayOf(2, 5, 7, 10)
        val bonusCoef = (100 + coinBonusPercentage) / 100
        val reward = rewards[createRandom().nextInt(rewards.size)]
        return (reward * bonusCoef).toInt()
    }

    private fun experience(xpBonusPercentage: Float): Int {
        val rewards = intArrayOf(5, 10, 15, 20, 30)
        val bonusCoef = (100 + xpBonusPercentage) / 100
        val reward = rewards[createRandom().nextInt(rewards.size)]
        return (reward * bonusCoef).toInt()
    }

    private fun bounty(bountyBonusPercentage: Float): Quest.Bounty {
        val bountyBonus = Constants.QUEST_BOUNTY_DROP_PERCENTAGE * (bountyBonusPercentage / 100)
        val totalBountyPercentage = Constants.QUEST_BOUNTY_DROP_PERCENTAGE + bountyBonus

        val random = createRandom().nextDouble()
        if (random > totalBountyPercentage / 100) {
            return Quest.Bounty.None
        } else {
            return chooseBounty()
        }
    }

    private fun chooseBounty(): Quest.Bounty.Food {
        val foods = Food.values() + Food.POOP + Food.POOP + Food.BEER
        val index = createRandom().nextInt(foods.size)
        return Quest.Bounty.Food(foods[index])
    }

    private fun createRandom(): Random {
        val random = Random()
        randomSeed?.let { random.setSeed(it) }
        return random
    }

    sealed class Params {
        data class WithQuest(val quest: Quest) : Params()
        data class WithQuestId(val questId: String) : Params()
    }
}