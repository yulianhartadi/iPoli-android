package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.ReminderScheduler
import io.ipoli.android.quest.show.job.TimerCompleteScheduler
import timber.log.Timber

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
        try {
            if (quest.isFromRepeatingQuest) {
                questRepository.removeFromRepeatingQuest(parameters, "${quest.repeatingQuestId!!}*")
            } else {
                questRepository.remove(parameters)
            }
        } catch (e: Throwable) {
            Timber.e(e)
        }
        reminderScheduler.schedule()
    }
}