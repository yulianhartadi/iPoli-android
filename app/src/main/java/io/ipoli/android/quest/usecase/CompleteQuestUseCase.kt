package io.ipoli.android.quest.usecase

import io.ipoli.android.Constants
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.pet.Food
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.QuestCompleteScheduler
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.reminder.ReminderScheduler
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/27/17.
 */
class CompleteQuestUseCase(
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository,
    private val reminderScheduler: ReminderScheduler,
    private val questCompleteScheduler: QuestCompleteScheduler,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val randomSeed: Long? = null
) : UseCase<String, Quest> {
    override fun execute(parameters: String): Quest {

        require(parameters.isNotEmpty(), { "questId cannot be empty" })

        val pet = playerRepository.find()!!.pet

        val quest = questRepository.findById(parameters)!!
        val experience = quest.experience ?: experience(pet.experienceBonus)
        val coins = quest.coins ?: coins(pet.coinBonus)
        val bounty = quest.bounty ?: bounty(pet.bountyBonus)
        val newQuest = quest.copy(
            completedAtDate = LocalDate.now(),
            completedAtTime = Time.now(),
            experience = experience,
            coins = coins,
            bounty = bounty
        )

        questRepository.save(newQuest)

        val quests = questRepository.findNextQuestsToRemind()
        if (quests.isNotEmpty()) {
            reminderScheduler.schedule(quests.first().reminder!!.toMillis())
        }

        rewardPlayerUseCase.execute(newQuest)

        questCompleteScheduler.schedule(parameters)
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
        return Quest.Bounty.Food(Food.BANANA)
    }

    private fun createRandom(): Random {
        val random = Random()
        randomSeed?.let { random.setSeed(randomSeed) }
        return random
    }
}