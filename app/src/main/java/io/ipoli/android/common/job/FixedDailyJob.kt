package io.ipoli.android.common.job

import com.crashlytics.android.Crashlytics
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.BuildConfig
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.toMillis
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

abstract class FixedDailyJob(private val tag: String) : Job() {

    override fun onRunJob(params: Params): Result {
        try {
            doRunJob(params)
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Timber.e(e)
            } else {
                Crashlytics.logException(e)
            }
        }

        val scheduleAt = Time.of(params.extras.getInt("minuteOfDay", -1))

        val nextSchedule = LocalDateTime.of(
            LocalDate.now().plusDays(1),
            LocalTime.of(scheduleAt.hours, scheduleAt.getMinutes())
        ).toMillis()

        JobRequest.Builder(tag)
            .setUpdateCurrent(true)
            .setExtras(PersistableBundleCompat().apply {
                putInt("minuteOfDay", scheduleAt.toMinuteOfDay())
            })
            .setExact(nextSchedule - System.currentTimeMillis())
            .build()
            .schedule()
        return Result.SUCCESS
    }

    abstract fun doRunJob(params: Params): Result
}

object FixedDailyJobScheduler {

    fun schedule(tag: String, scheduleAt: Time) {

        val nextScheduled = if (scheduleAt <= Time.now()) {
            LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.of(scheduleAt.hours, scheduleAt.getMinutes())
            ).toMillis()
        } else {
            LocalDateTime.of(
                LocalDate.now(),
                LocalTime.of(scheduleAt.hours, scheduleAt.getMinutes())
            ).toMillis()
        }

        val scheduleMillis = nextScheduled - System.currentTimeMillis()

        JobRequest.Builder(tag)
            .setUpdateCurrent(true)
            .setExtras(PersistableBundleCompat().apply {
                putInt("minuteOfDay", scheduleAt.toMinuteOfDay())
            })
            .setExact(scheduleMillis)
            .build()
            .schedule()
    }
}