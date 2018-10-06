package io.ipoli.android.achievement.job

import io.ipoli.android.MyPoliApp
import io.ipoli.android.achievement.usecase.UpdateAchievementProgressUseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.BackgroundModule
import io.ipoli.android.common.job.FixedDailyJob
import io.ipoli.android.common.job.FixedDailyJobScheduler
import io.ipoli.android.friends.usecase.SavePostsUseCase
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Kapsule

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 06/08/2018.
 */

class UpdateAchievementProgressJob : FixedDailyJob(UpdateAchievementProgressJob.TAG) {

    override fun doRunJob(params: Params): Result {
        val kap = Kapsule<BackgroundModule>()
        val playerRepository by kap.required { playerRepository }
        val updateAchievementProgressUseCase by kap.required { updateAchievementProgressUseCase }
        val savePostsUseCase by kap.required { savePostsUseCase }
        kap.inject(MyPoliApp.backgroundModule(context))

        val stats = playerRepository.find()!!.statistics
        val newStats =
            updateAchievementProgressUseCase.execute(UpdateAchievementProgressUseCase.Params())
        if (stats.dailyChallengeCompleteStreak.count > 0 && newStats.dailyChallengeCompleteStreak.count == 0L) {
            savePostsUseCase.execute(SavePostsUseCase.Params.DailyChallengeFailed())
        }

        return Result.SUCCESS
    }

    companion object {
        const val TAG = "job_update_achievement_progress"
    }
}

interface UpdateAchievementProgressScheduler {
    fun schedule()
}

class AndroidUpdateAchievementProgressScheduler : UpdateAchievementProgressScheduler {

    override fun schedule() {
        GlobalScope.launch(Dispatchers.IO) {
            FixedDailyJobScheduler.schedule(UpdateAchievementProgressJob.TAG, Time.at(0, 1))
        }
    }

}