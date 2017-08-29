package io.ipoli.android.common.jobservice

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import io.ipoli.android.reward.jobservice.RemoveRewardJobService
import java.util.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
class JobQueue(private val context: Context) {

    companion object {
        val DEFAULT_EXECUTION_DELAY_MILLIS = 2500L
    }

    fun add(data: PersistableBundle, service: Class<*>, jobId: Int = Random().nextInt(), executionDelayMillis: Long = DEFAULT_EXECUTION_DELAY_MILLIS): Int {
        val jobScheduler = getJobScheduler()
        val job = JobInfo.Builder(jobId,
            ComponentName(context, service))
            .setMinimumLatency(executionDelayMillis)
            .setOverrideDeadline(executionDelayMillis)
            .setExtras(data)
            .build()
        jobScheduler.schedule(job)
        return jobId
    }

    private fun getJobScheduler(): JobScheduler =
        context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    fun remove(jobId: Int) {
        getJobScheduler().cancel(jobId)
    }
}