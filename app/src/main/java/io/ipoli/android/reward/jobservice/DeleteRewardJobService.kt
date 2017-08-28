package io.ipoli.android.reward.jobservice

import android.app.job.JobParameters
import android.app.job.JobService
import io.ipoli.android.reward.RealmRewardRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/28/17.
 */
class DeleteRewardJobService : JobService() {


    override fun onStartJob(jobParameters: JobParameters): Boolean {

        val rewardId = jobParameters.extras.getString("reward_id")
        val rewardRepo = RealmRewardRepository()

        rewardRepo.delete(rewardId)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { jobFinished(jobParameters, false) },
                { error ->
                    Timber.d(error)
                    jobFinished(jobParameters, true)
                }
            )

        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean = false

}