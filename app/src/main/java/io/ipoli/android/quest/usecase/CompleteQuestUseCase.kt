package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
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
    private val randomSeed: Long = System.currentTimeMillis()
) : UseCase<String, Quest> {
    override fun execute(parameters: String): Quest {

        require(parameters.isNotEmpty(), { "questId cannot be empty" })

        val pet = playerRepository.find()!!.pet

        val quest = questRepository.findById(parameters)!!
        val experience = quest.experience ?: xpForQuest(pet.experienceBonus)
        val coins = quest.coins ?: coinsForQuest(pet.coinBonus)
        val newQuest = quest.copy(
            completedAtDate = LocalDate.now(),
            completedAtTime = Time.now(),
            experience = experience,
            coins = coins
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

    private fun coinsForQuest(coinBonusPercentage: Float): Int {
        val rewards = intArrayOf(2, 5, 7, 10)
        val bonusCoef = (100 + coinBonusPercentage) / 100
        val reward = rewards[Random(randomSeed).nextInt(rewards.size)]
        return (reward * bonusCoef).toInt()
    }

    private fun xpForQuest(xpBonusPercentage: Float): Int {
        val rewards = intArrayOf(5, 10, 15, 20, 30)
        val bonusCoef = (100 + xpBonusPercentage) / 100
        val reward = rewards[Random(randomSeed).nextInt(rewards.size)]
        return (reward * bonusCoef).toInt()
    }
}