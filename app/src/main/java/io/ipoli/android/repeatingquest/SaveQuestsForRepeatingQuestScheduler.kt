package io.ipoli.android.repeatingquest

import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobRequest
import io.ipoli.android.common.di.BackgroundModule
import io.ipoli.android.myPoliApp
import io.ipoli.android.repeatingquest.usecase.SaveQuestsForRepeatingQuestUseCase
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule
import java.util.concurrent.TimeUnit


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/03/2018.
 */
class SaveQuestsForRepeatingQuestJob : DailyJob(), Injects<BackgroundModule> {

    override fun onRunDailyJob(params: Params): DailyJobResult {
        val kap = Kapsule<BackgroundModule>()
        val repeatingQuestRepository by kap.required { repeatingQuestRepository }
        val saveQuestsForRepeatingQuestUseCase by kap.required { saveQuestsForRepeatingQuestUseCase }
        kap.inject(myPoliApp.backgroundModule(context))

        val rqs = repeatingQuestRepository.findAllActive()
        rqs.forEach {
            val currentPeriod = it.repeatPattern.periodRangeFor(LocalDate.now())
            val nextPeriodFirstDate = currentPeriod.end.plusDays(1)
            val end = it.repeatPattern.periodRangeFor(nextPeriodFirstDate).end
            saveQuestsForRepeatingQuestUseCase.execute(
                SaveQuestsForRepeatingQuestUseCase.Params(
                    repeatingQuest = it,
                    start = LocalDate.now(),
                    end = end
                )
            )
        }

        return DailyJobResult.SUCCESS
    }

    companion object {
        const val TAG = "save_quests_for_repeating_quest_tag"
    }
}

class AndroidSaveQuestsForRepeatingQuestScheduler : SaveQuestsForRepeatingQuestScheduler {
    override fun schedule() {

        DailyJob.schedule(
            JobRequest.Builder(SaveQuestsForRepeatingQuestJob.TAG)
                .setUpdateCurrent(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true),
            0,
            TimeUnit.HOURS.toMillis(12)
        )
    }

}

interface SaveQuestsForRepeatingQuestScheduler {
    fun schedule()
}