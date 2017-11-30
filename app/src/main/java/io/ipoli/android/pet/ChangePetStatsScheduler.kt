package io.ipoli.android.pet

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.di.JobModule
import io.ipoli.android.iPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule
import java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/30/17.
 */

class ChangePetStatsJob : Job(), Injects<ControllerModule> {

    override fun onRunJob(params: Params): Result {
        val kap = Kapsule<JobModule>()
        val changePetStatsUseCase by kap.required { changePetStatsUseCase }
        kap.inject(iPoliApp.jobModule(context))

        val time = Time.of(params.extras.getInt("changeStatsTime", 0))
        changePetStatsUseCase.execute(time)

        return Result.SUCCESS
    }

    companion object {
        val TAG = "job_change_pet_stats_tag"
    }
}

class AndroidJobChangePetStatsScheduler : ChangePetStatsScheduler {
    override fun schedule() {
        val currentTime = Time.now()
        val morning = Time.atHours(9)
        val afternoon = Time.atHours(14)
        val evening = Time.atHours(19)

        val changeStatsTime = when {
            currentTime.isBetween(morning - 30, afternoon - 1) -> afternoon
            currentTime.isBetween(afternoon - 30, evening - 1) -> evening
            else -> morning
        }

        val bundle = PersistableBundleCompat()
        bundle.putInt("changeStatsTime", changeStatsTime.toMinuteOfDay())
        JobRequest.Builder(ChangePetStatsJob.TAG)
            .setExtras(bundle)
            .setExact(TimeUnit.MINUTES.toMillis(currentTime.minutesTo(changeStatsTime).toLong()))
            .build()
            .schedule()
    }
}

interface ChangePetStatsScheduler {
    fun schedule()
}