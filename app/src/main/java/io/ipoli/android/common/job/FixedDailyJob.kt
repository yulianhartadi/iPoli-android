package io.ipoli.android.common.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.common.datetime.DateUtils.ZONE_UTC
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.toMillis
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

abstract class FixedDailyJob(private val tag: String) : Job() {

    override fun onRunJob(params: Params): Result {
        val r = doRunJob(params)
        if (r == Result.SUCCESS) {

            val scheduleAt = Time.of(params.extras.getInt("minuteOfDay", -1))

            val nextSchedule = LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.of(scheduleAt.hours, scheduleAt.getMinutes())
            ).toMillis(ZONE_UTC)

            JobRequest.Builder(tag)
                .setUpdateCurrent(true)
                .setExact(nextSchedule - System.currentTimeMillis())
                .build()
                .schedule()
        }
        return r
    }

    abstract fun doRunJob(params: Params): Result
}

object FixedDailyJobScheduler {

    fun schedule(tag: String, scheduleAt: Time) {

        val nextSchedule = LocalDateTime.of(
            LocalDate.now(),
            LocalTime.of(scheduleAt.hours, scheduleAt.getMinutes())
        ).toMillis(ZONE_UTC)

        val builder = JobRequest.Builder(tag).apply {
            setUpdateCurrent(true)
            setExtras(PersistableBundleCompat().apply {
                putInt("minuteOfDay", scheduleAt.toMinuteOfDay())
            })
            val scheduleMillis = nextSchedule - System.currentTimeMillis()
            if (scheduleMillis < 0) {
                startNow()
            } else {
                setExact(scheduleMillis)
            }
        }

        builder
            .build()
            .schedule()
    }
}