package io.ipoli.android.player

import android.view.ContextThemeWrapper
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import io.ipoli.android.R
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.di.JobModule
import io.ipoli.android.iPoliApp
import io.ipoli.android.player.view.LevelUpPopup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */
class LevelUpJob : Job(), Injects<ControllerModule> {
    override fun onRunJob(params: Params): Result {

        val kap = Kapsule<JobModule>()
        val findPlayerLevelUseCase by kap.required { findPlayerLevelUseCase }
        kap.inject(iPoliApp.jobModule(context))

        val playerLevel = findPlayerLevelUseCase.execute(Unit)

        val c = ContextThemeWrapper(context, R.style.Theme_iPoli)

        launch(UI) {
            LevelUpPopup(playerLevel).show(c)
        }

        return Result.SUCCESS
    }

    companion object {
        val TAG = "job_level_up_tag"
    }
}

interface LevelUpScheduler {
    fun schedule()
}

class AndroidLevelUpScheduler : LevelUpScheduler {
    override fun schedule() {
        JobRequest.Builder(LevelUpJob.TAG)
            .startNow()
            .build()
            .schedule()
    }
}