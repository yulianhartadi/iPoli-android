package mypoli.android.player

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import mypoli.android.common.di.ControllerModule
import mypoli.android.common.view.asThemedWrapper
import mypoli.android.player.view.LevelUpPopup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
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