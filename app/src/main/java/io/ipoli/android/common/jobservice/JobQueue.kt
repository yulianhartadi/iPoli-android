package io.ipoli.android.common.jobservice

import io.ipoli.android.reward.Reward
import io.ipoli.android.reward.RewardRepository
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
open class JobQueue(private val rewardRepository: RewardRepository) {

    companion object {
        val DEFAULT_EXECUTION_DELAY_MILLIS = 2500L
    }

    private val jobDisposables: MutableMap<String, Disposable> = mutableMapOf()

    open fun save(reward: Reward): String = add<Reward>(rewardRepository.save(reward).toObservable())

    fun <T> add(job: Observable<T>): String =
        addDelayed(job, 0)

    fun <T> addDelayed(job: Observable<T>, executionDelayMillis: Long = DEFAULT_EXECUTION_DELAY_MILLIS): String {
        val jobId = UUID.randomUUID().toString()
        val disposable = job
            .delaySubscription(executionDelayMillis, TimeUnit.MILLISECONDS)
            .doOnDispose {
                if (jobId in jobDisposables) {
                    jobDisposables.remove(jobId)
                }
            }
            .doOnComplete {
                if (jobId in jobDisposables) {
                    jobDisposables.remove(jobId)
                }
            }
            .subscribe()
        jobDisposables[jobId] = disposable
        return jobId
    }

    fun remove(jobId: String) {
        jobDisposables[jobId]?.dispose()
    }
}