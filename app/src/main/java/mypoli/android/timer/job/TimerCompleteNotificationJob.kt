package mypoli.android.timer.job

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import mypoli.android.Constants
import mypoli.android.R
import mypoli.android.common.datetime.Interval
import mypoli.android.common.datetime.Minute
import mypoli.android.common.di.ControllerModule
import mypoli.android.common.di.SimpleModule
import mypoli.android.myPoliApp
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.quest.TimeRange
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule
import java.util.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/22/2018.
 */
class TimerCompleteNotificationJob : Job(), Injects<ControllerModule> {

    @SuppressLint("NewApi")
    override fun onRunJob(params: Job.Params): Job.Result {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val kap = Kapsule<SimpleModule>()
        val questRepository by kap.required { questRepository }
        val findPetUseCase by kap.required { findPetUseCase }
        kap.inject(myPoliApp.simpleModule(context))

        val questId = params.extras.getString("questId", "")
        require(questId.isNotEmpty())
        val quest = questRepository.findById(questId)
        val pet = findPetUseCase.execute(Unit)

        val (name, message) = if (quest!!.actualStart != null) {
            Pair(
                "Quest complete",
                "${quest.name} is all done!"
            )
        } else {
            val timeRange = quest.pomodoroTimeRanges.last()
            if (timeRange.type == TimeRange.Type.WORK) {
                Pair(
                    "Pomodoro complete",
                    "Your pomodoro is added to ${quest.name}. Ready for a break?"
                )
            } else {
                if (quest.hasCompletedAllTimeRanges()) {
                    Pair(
                        "Break complete",
                        "${quest.name} is all done!"
                    )
                } else {
                    Pair(
                        "Break complete",
                        "Ready to work on ${quest.name}?"
                    )
                }
            }
        }


        val petAvatar = AndroidPetAvatar.valueOf(pet.avatar.name)

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, petAvatar.headImage))
            .setContentTitle(name)
            .setContentText(message)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        val notificationId = Random().nextInt()
        notificationManager.notify(notificationId, notification)

        return Job.Result.SUCCESS
    }

    companion object {
        val TAG = "job_timer_complete_notification_tag"
    }
}

interface TimerCompleteScheduler {
    fun schedule(questId: String, after: Interval<Minute>)
}

class AndroidJobTimerCompleteScheduler : TimerCompleteScheduler {

    override fun schedule(questId: String, after: Interval<Minute>) {
        JobManager.instance().cancelAllForTag(TimerCompleteNotificationJob.TAG)

        val bundle = PersistableBundleCompat()
        bundle.putString("questId", questId)
        JobRequest.Builder(TimerCompleteNotificationJob.TAG)
            .setExtras(bundle)
//            .setExact(after.asMilliseconds.longValue)
            .setExact(1000)
            .build()
            .schedule()
    }
}