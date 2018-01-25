package mypoli.android.quest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.quest.job.ReminderScheduler
import mypoli.android.timer.job.TimerCompleteScheduler

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/9/17.
 */
class RemoveQuestUseCase(
    private val questRepository: QuestRepository,
    private val timerCompleteScheduler: TimerCompleteScheduler,
    private val reminderScheduler: ReminderScheduler
) : UseCase<String, Unit> {
    override fun execute(parameters: String) {
        if (parameters.isEmpty()) {
            return
        }
        val quest = questRepository.findById(parameters)!!
        if (quest.isStarted) {
            timerCompleteScheduler.cancelAll()
        }
        questRepository.remove(parameters)
        val quests = questRepository.findNextQuestsToRemind()
        if (quests.isNotEmpty()) {
            val reminder = quests[0].reminder!!
            reminderScheduler.schedule(reminder.toMillis())
        }
    }
}