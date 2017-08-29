package io.ipoli.android.reward.list.usecase

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.common.jobservice.JobQueue
import io.ipoli.android.reward.jobservice.RemoveRewardJobService
import io.ipoli.android.reward.list.RewardListPartialChange
import io.ipoli.android.reward.list.RewardViewModel
import io.reactivex.Observable
import java.util.*


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/17.
 */
class RemoveRewardFromListUseCase(private val jobQueue: JobQueue) : BaseRxUseCase<RemoveRewardFromListUseCase.RemoveParameters, RewardListPartialChange>() {

    private var jobId = 0

    override fun createObservable(parameters: RemoveParameters): Observable<RewardListPartialChange> {
        return Observable.defer {
            val indexOfRemovedReward = parameters.rewards.indexOf(parameters.rewardToRemove)
            val newList = parameters.rewards.filter { it != parameters.rewardToRemove }
            val data = PersistableBundle()
            data.putString("reward_id", parameters.rewardToRemove.id)
            jobId = jobQueue.add(data, RemoveRewardJobService::class.java)
//            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

//            jobId = Random().nextInt()
//            val job = JobInfo.Builder(jobId,
//                ComponentName(context, RemoveRewardJobService::class.java))
//                .setMinimumLatency(2500)
//                .setOverrideDeadline(2500)
//                .setExtras(data)
//                .build()
//            jobScheduler.schedule(job)
            if (newList.isEmpty()) {
                Observable.just(RewardListPartialChange.Empty(parameters.rewardToRemove, indexOfRemovedReward))
            } else {
                Observable.just(RewardListPartialChange.RewardRemoved(newList, parameters.rewardToRemove, indexOfRemovedReward))
            }
        }.onErrorReturn { RewardListPartialChange.Error() }
    }

    data class RemoveParameters(
        val rewards: List<RewardViewModel>,
        val rewardToRemove: RewardViewModel
    )

    data class UndoParameters(
        val rewards: List<RewardViewModel>,
        val removedReward: RewardViewModel,
        val removedRewardIndex: Int
    )

    fun undo(parameters: UndoParameters): Observable<RewardListPartialChange> =
        Observable.defer {
            //            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//            jobScheduler.cancel(jobId)
            jobQueue.remove(jobId)
            val newList = parameters.rewards.toMutableList()
            newList.add(parameters.removedRewardIndex, parameters.removedReward)
            Observable.just(RewardListPartialChange.UndoRemovedReward(newList))
        }
            .cast(RewardListPartialChange::class.java)
            .onErrorReturn { RewardListPartialChange.Error() }
}