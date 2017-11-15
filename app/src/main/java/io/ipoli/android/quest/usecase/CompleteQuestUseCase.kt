package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
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
    private val reminderScheduler: ReminderScheduler,
    private val questCompleteScheduler: QuestCompleteScheduler
) : UseCase<String, Quest> {
    override fun execute(parameters: String): Quest {

        require(parameters.isNotEmpty(), { "questId cannot be empty" })

        val quest = questRepository.findById(parameters)!!
        val experience = quest.experience ?: xpForQuest()
        val newQuest = quest.copy(
            completedAtDate = LocalDate.now(),
            completedAtTime = Time.now(),
            experience = experience
        )

        questRepository.save(newQuest)

        val quests = questRepository.findNextQuestsToRemind()
        if (quests.isNotEmpty()) {
            reminderScheduler.schedule(quests.first().reminder!!.toMillis())
        }

        questCompleteScheduler.schedule(parameters)

        return newQuest
    }

    private fun xpForQuest(): Int {
        val rewards = intArrayOf(5, 10, 15, 20, 30)
        return rewards[Random().nextInt(rewards.size)] * TEMP_BONUS_MULTIPLIER
    }

    companion object {
        const val TEMP_BONUS_MULTIPLIER = 2
    }
}