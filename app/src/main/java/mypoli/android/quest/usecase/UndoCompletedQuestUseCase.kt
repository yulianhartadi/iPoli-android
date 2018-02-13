package mypoli.android.quest.usecase

import mypoli.android.common.SimpleReward
import mypoli.android.common.UseCase
import mypoli.android.player.usecase.RemoveRewardFromPlayerUseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.quest.job.ReminderScheduler

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
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

        val reminderTime = questRepository.findNextReminderTime()
        reminderTime?.let {
            reminderScheduler.schedule(it)
        }

        removeRewardFromPlayerUseCase.execute(SimpleReward.of(newQuest))

        return newQuest
    }
}