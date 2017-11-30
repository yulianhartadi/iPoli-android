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

class LowerPetStatsJob : Job(), Injects<ControllerModule> {

    override fun onRunJob(params: Params): Result {
        val kap = Kapsule<JobModule>()
        val changePetStatsUseCase by kap.required { lowerPetStatsUseCase }
        val lowerPetStatsScheduler by kap.required { lowerPetStatsScheduler }
        kap.inject(iPoliApp.jobModule(context))

        val time = Time.of(params.extras.getInt("lowerStatsTime", 0))
        changePetStatsUseCase.execute(time)

        lowerPetStatsScheduler.schedule()
        return Result.SUCCESS
    }

    companion object {
        val TAG = "job_lower_pet_stats_tag"
    }
}

class AndroidJobLowerPetStatsScheduler : LowerPetStatsScheduler {
    override fun schedule() {
        val currentTime = Time.now()
        val morning = Time.atHours(9)
        val afternoon = Time.atHours(14)
        val evening = Time.atHours(19)

        val lowerStatsTime = when {
            currentTime.isBetween(morning - 30, afternoon - 1) -> afternoon
            currentTime.isBetween(afternoon - 30, evening - 1) -> evening
            else -> morning
        }

        val bundle = PersistableBundleCompat()
        bundle.putInt("lowerStatsTime", lowerStatsTime.toMinuteOfDay())
        JobRequest.Builder(LowerPetStatsJob.TAG)
            .setExtras(bundle)
            .setExact(TimeUnit.MINUTES.toMillis(currentTime.minutesTo(lowerStatsTime).toLong()))
            .build()
            .schedule()
    }
}

interface LowerPetStatsScheduler {
    fun schedule()
}