package io.ipoli.android.pet

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.job.FixedDailyJob
import io.ipoli.android.common.job.FixedDailyJobScheduler
import io.ipoli.android.myPoliApp
import io.ipoli.android.pet.usecase.LowerPetStatsUseCase
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/30/17.
 */

class LowerPetStatsJob : FixedDailyJob(LowerPetStatsJob.TAG), Injects<Module> {

    override fun doRunJob(params: Params): Result {
        val kap = Kapsule<Module>()
        val changePetStatsUseCase by kap.required { lowerPetStatsUseCase }
        val eventLogger by kap.required { eventLogger }
        kap.inject(myPoliApp.module(context))

        eventLogger.logEvent("lower_pet_stats", mapOf("lowerStatsTime" to Time.now()))

        changePetStatsUseCase.execute(LowerPetStatsUseCase.Params(LocalDate.now().minusDays(1)))

        return Result.SUCCESS
    }

    companion object {
        const val TAG = "job_lower_pet_stats_tag"
    }
}

class AndroidJobLowerPetStatsScheduler : LowerPetStatsScheduler {
    override fun schedule() {
        FixedDailyJobScheduler.schedule(LowerPetStatsJob.TAG, Time.at(0, 10))
    }
}

interface LowerPetStatsScheduler {
    fun schedule()
}