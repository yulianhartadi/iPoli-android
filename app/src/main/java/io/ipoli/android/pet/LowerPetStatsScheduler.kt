package io.ipoli.android.pet

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.Constants
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.Module
import io.ipoli.android.myPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule
import java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/30/17.
 */

class LowerPetStatsJob : Job(), Injects<Module> {

    override fun onRunJob(params: Params): Result {
        val kap = Kapsule<Module>()
        val changePetStatsUseCase by kap.required { lowerPetStatsUseCase }
        val lowerPetStatsScheduler by kap.required { lowerPetStatsScheduler }
        kap.inject(myPoliApp.module(context))

        val time = Time.of(params.extras.getInt("lowerStatsTime", 0))
        changePetStatsUseCase.execute(time)

        lowerPetStatsScheduler.schedule()
        return Result.SUCCESS
    }

    companion object {
        const val TAG = "job_lower_pet_stats_tag"
    }
}

class AndroidJobLowerPetStatsScheduler : LowerPetStatsScheduler {
    override fun schedule() {
        val currentTime = Time.now()
        val morning = Constants.CHANGE_PET_STATS_MORNING_TIME
        val afternoon = Constants.CHANGE_PET_STATS_AFTERNOON_TIME
        val evening = Constants.CHANGE_PET_STATS_EVENING_TIME

        val lowerStatsTime = when {
            currentTime.isBetween(morning - 30, afternoon - 1) -> afternoon
            currentTime.isBetween(afternoon - 30, evening - 1) -> evening
            else -> morning
        }

        val bundle = PersistableBundleCompat()
        bundle.putInt("lowerStatsTime", lowerStatsTime.toMinuteOfDay())
        JobRequest.Builder(LowerPetStatsJob.TAG)
            .setExtras(bundle)
            .setUpdateCurrent(true)
            .setExact(TimeUnit.MINUTES.toMillis(currentTime.minutesTo(lowerStatsTime).toLong()))
            .build()
            .schedule()
    }
}

interface LowerPetStatsScheduler {
    fun schedule()
}