package mypoli.android.quest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.ReminderScheduler

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/9/17.
 */
class RemoveQuestUseCase(private val questRepository: QuestRepository, private val reminderScheduler: ReminderScheduler) : UseCase<String, Unit> {
    override fun execute(parameters: String) {
        if (parameters.isEmpty()) {
            return
        }
        questRepository.remove(parameters)
        val quests = questRepository.findNextQuestsToRemind()
        if (quests.isNotEmpty()) {
            val reminder = quests[0].reminder!!
            reminderScheduler.schedule(reminder.toMillis())
        }
    }
}