package io.ipoli.android.dailychallenge.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.MyPoliApp
import io.ipoli.android.achievement.usecase.UnlockAchievementsUseCase
import io.ipoli.android.achievement.usecase.UpdatePlayerStatsUseCase
import io.ipoli.android.common.datetime.seconds
import io.ipoli.android.common.di.BackgroundModule
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.dailychallenge.DailyChallengeCompletePopup
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Kapsule

class DailyChallengeCompleteJob : Job() {

    override fun onRunJob(params: Params): Result {

        val kap = Kapsule<BackgroundModule>()
        val unlockAchievement by kap.required { unlockAchievementsUseCase }
        val playerRepository by kap.required { playerRepository }
        kap.inject(MyPoliApp.backgroundModule(context))

        val coins = params.extras.getInt(KEY_COINS, -1)
        val experience = params.extras.getInt(KEY_EXPERIENCE, -1)

        require(coins > 0) { "DailyChallengeCompleteJob has incorrect coins param $coins" }
        require(
            experience > 0
        ) { "DailyChallengeCompleteJob has incorrect experience param $experience" }

        val c = context.asThemedWrapper()
        GlobalScope.launch(Dispatchers.Main) {
            DailyChallengeCompletePopup(experience, coins).show(c)
        }

        unlockAchievement.execute(
            UnlockAchievementsUseCase.Params(
                playerRepository.find()!!,
                UpdatePlayerStatsUseCase.Params.EventType.DailyChallengeCompleted
            )
        )

        return Result.SUCCESS
    }

    companion object {
        const val TAG = "job_daily_challenge_complete_tag"

        const val KEY_EXPERIENCE = "EXPERIENCE"
        const val KEY_COINS = "COINS"
    }

}

interface DailyChallengeCompleteScheduler {
    fun schedule(experience: Int, coins: Int)
}

class AndroidDailyChallengeCompleteScheduler : DailyChallengeCompleteScheduler {

    override fun schedule(experience: Int, coins: Int) {

        val params = PersistableBundleCompat()
        params.putInt(DailyChallengeCompleteJob.KEY_EXPERIENCE, experience)
        params.putInt(DailyChallengeCompleteJob.KEY_COINS, coins)

        JobRequest.Builder(DailyChallengeCompleteJob.TAG)
            .setExtras(params)
            .setUpdateCurrent(true)
            .setExact(2.seconds.millisValue)
            .build()
            .schedule()
    }

}