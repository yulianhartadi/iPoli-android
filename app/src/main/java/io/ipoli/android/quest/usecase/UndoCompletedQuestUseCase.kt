package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.player.usecase.RemoveRewardFromPlayerUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.reminder.ReminderScheduler

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/27/17.
 */
class UndoCompletedQuestUseCase(
    private val questRepository: QuestRepository,
    private val reminderScheduler: ReminderScheduler,
    private val removeRewardFromPlayerUseCase: RemoveRewardFromPlayerUseCase
) : UseCase<String, Quest> {
    override fun execute(parameters: String): Quest {
        require(parameters.isNotEmpty(), { "questId cannot be empty" })

        val newQuest = questRepository.findById(parameters)!!.copy(
            completedAtDate = null,
            completedAtTime = null
        )
        questRepository.save(newQuest)

        val quests = questRepository.findNextQuestsToRemind()
        if (quests.isNotEmpty()) {
            reminderScheduler.schedule(quests.first().reminder!!.toMillis())
        }

        removeRewardFromPlayerUseCase.execute(newQuest)

        return newQuest
    }
}