package io.ipoli.android.habit.job

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.job.FixedDailyJob
import io.ipoli.android.common.job.FixedDailyJobScheduler
import io.ipoli.android.habit.usecase.UpdateHabitStreaksUseCase
import io.ipoli.android.myPoliApp
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Kapsule

class UpdateHabitStreaksJob : FixedDailyJob(UpdateHabitStreaksJob.TAG) {

    override fun doRunJob(params: Params): Result {
        val kap = Kapsule<Module>()
        val updateHabitStreaksUseCase by kap.required { updateHabitStreaksUseCase }
        kap.inject(myPoliApp.module(context))
        updateHabitStreaksUseCase.execute(
            UpdateHabitStreaksUseCase.Params(
                LocalDate.now().minusDays(
                    1
                )
            )
        )
        return Result.SUCCESS
    }

    companion object {
        const val TAG = "job_update_habit_streaks_tag"
    }
}

interface UpdateHabitStreaksScheduler {
    fun schedule()
}

class AndroidUpdateHabitStreaksScheduler : UpdateHabitStreaksScheduler {
    override fun schedule() {

        FixedDailyJobScheduler.schedule(UpdateHabitStreaksJob.TAG, Time.at(0, 1))
    }
}