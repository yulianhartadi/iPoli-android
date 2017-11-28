package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.LevelUpScheduler
import io.ipoli.android.player.persistence.PlayerRepository
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
    private val levelUpScheduler: LevelUpScheduler,
    private val randomSeed: Long = System.currentTimeMillis()
) : UseCase<String, Quest> {
    override fun execute(parameters: String): Quest {

        require(parameters.isNotEmpty(), { "questId cannot be empty" })

        val quest = questRepository.findById(parameters)!!
        val experience = quest.experience ?: xpForQuest()
        val coins = quest.coins ?: coinsForQuest()
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

        val player = playerRepository.find()
        requireNotNull(player)
        val newPlayer = player!!.addExperience(experience).addCoins(coins)

        if (newPlayer.level != player.level) {
            levelUpScheduler.schedule()
        } else {
            questCompleteScheduler.schedule(parameters)
        }

        playerRepository.save(newPlayer)
        return newQuest
    }

    private fun coinsForQuest(): Int {
        val rewards = intArrayOf(2, 5, 7, 10)
        return rewards[Random(randomSeed).nextInt(rewards.size)] * TEMP_BONUS_MULTIPLIER
    }

    private fun xpForQuest(): Int {
        val rewards = intArrayOf(5, 10, 15, 20, 30)
        return rewards[Random(randomSeed).nextInt(rewards.size)] * TEMP_BONUS_MULTIPLIER
    }

    companion object {
        const val TEMP_BONUS_MULTIPLIER = 2
    }
}