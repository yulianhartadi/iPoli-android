package mypoli.android.rate

import android.preference.PreferenceManager
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.common.di.Module
import mypoli.android.common.view.asThemedWrapper
import space.traversal.kapsule.Injects
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/15/17.
 */
class RatePopupJob : Job(), Injects<Module> {

    override fun onRunJob(params: Params): Result {
        val pm = PreferenceManager.getDefaultSharedPreferences(context)
        val shouldShowRateDialog = pm.getBoolean(Constants.KEY_SHOULD_SHOW_RATE_DIALOG, true)
        val appRun = pm.getInt(Constants.KEY_APP_RUN_COUNT, 0)
        val shouldShowRandom = Random().nextBoolean()

        if (!shouldShowRateDialog || appRun <= 2 || !shouldShowRandom) {
            return Result.SUCCESS
        }

        val c = context.asThemedWrapper()
        launch(UI) {
            RatePopup().show(c)
        }

        return Result.SUCCESS
    }

    companion object {
        val TAG = "job_rate_tag"
    }
}

interface RatePopupScheduler {
    fun schedule()
}

class AndroidRatePopupScheduler : RatePopupScheduler {

    override fun schedule() {
        JobRequest.Builder(RatePopupJob.TAG)
            .setExact(5000)
            .build()
            .schedule()
    }
}