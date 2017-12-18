package io.ipoli.android.player

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.player.view.LevelUpPopup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */
class LevelUpJob : Job(), Injects<ControllerModule> {
    override fun onRunJob(params: Params): Result {

        val c = context.asThemedWrapper()
        launch(UI) {
            LevelUpPopup().show(c)
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
            .setExact(1000)
            .build()
            .schedule()
    }
}