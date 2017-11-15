package io.ipoli.android.quest

import android.view.ContextThemeWrapper
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.R
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.di.JobModule
import io.ipoli.android.iPoliApp
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */

class QuestCompleteJob : Job(), Injects<ControllerModule> {
    override fun onRunJob(params: Params): Result {
        val questId = params.extras.getString("questId", "")

        val kap = Kapsule<JobModule>()
        val findCompletedQuestUseCase by kap.required { findCompletedQuestUseCase }
        kap.inject(iPoliApp.jobModule(context))

        val quest = findCompletedQuestUseCase.execute(questId)

        val c = ContextThemeWrapper(context, R.style.Theme_iPoli)

        launch(UI) {
            QuestCompletePopup(quest.experience!!).show(c)
        }

        return Result.SUCCESS
    }

    companion object {
        val TAG = "job_quest_complete_tag"
    }
}

interface QuestCompleteScheduler {
    fun schedule(questId: String)
}

class AndroidJobQuestCompleteScheduler : QuestCompleteScheduler {
    override fun schedule(questId: String) {
        val bundle = PersistableBundleCompat()
        bundle.putString("questId", questId)
        JobRequest.Builder(QuestCompleteJob.TAG)
            .setExtras(bundle)
            .startNow()
            .build()
            .schedule()
    }

}