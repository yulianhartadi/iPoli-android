package io.ipoli.android.reward.list.usecase

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.reward.jobservice.DeleteRewardJobService
import io.ipoli.android.reward.list.RewardListPartialChange
import io.ipoli.android.reward.list.RewardViewModel
import io.reactivex.Observable
import java.util.*


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/17.
 */
class RemoveRewardFromListUseCase(val context: Context) : BaseRxUseCase<RemoveRewardFromListUseCase.Parameters, RewardListPartialChange>() {
    override fun createObservable(parameters: Parameters): Observable<RewardListPartialChange> {
        return Observable.defer {
            val newList = parameters.rewards.filter { it != parameters.rewardToDelete }
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val data = PersistableBundle()
            data.putString("reward_id", parameters.rewardToDelete.id)
            val job = JobInfo.Builder(Random().nextInt(),
                ComponentName(context, DeleteRewardJobService::class.java))
                .setOverrideDeadline(0)
                .setExtras(data)
                .build()
            jobScheduler.schedule(job)
            if (newList.isEmpty()) {
                Observable.just(RewardListPartialChange.Empty())
            } else {
                Observable.just(RewardListPartialChange.RewardRemoved(newList))
            }
        }.onErrorReturn { RewardListPartialChange.Error() }
    }

    data class Parameters(
        val rewards: List<RewardViewModel>,
        val rewardToDelete: RewardViewModel
    )
}