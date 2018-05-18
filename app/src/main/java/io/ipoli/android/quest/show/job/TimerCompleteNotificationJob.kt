package io.ipoli.android.quest.show.job

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Second
import io.ipoli.android.common.di.Module
import io.ipoli.android.myPoliApp
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.quest.TimeRange
import io.ipoli.android.quest.receiver.CompleteQuestReceiver
import space.traversal.kapsule.Injects
import space.traversal.kapsule.Kapsule
import java.util.*


/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 01/22/2018.
 */
class TimerCompleteNotificationJob : Job(), Injects<Module> {

    @SuppressLint("NewApi")
    override fun onRunJob(params: Job.Params): Job.Result {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val kap = Kapsule<Module>()
        val questRepository by kap.required { questRepository }
        val findPetUseCase by kap.required { findPetUseCase }
        kap.inject(myPoliApp.module(context))

        val questId = params.extras.getString("questId", "")
        require(questId.isNotEmpty())
        val quest = questRepository.findById(questId)
        val pet = findPetUseCase.execute(Unit)

        val petAvatar = AndroidPetAvatar.valueOf(pet.avatar.name)

        val notificationBuilder =
            NotificationCompat.Builder(context, Constants.REMINDERS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, petAvatar.headImage))

        val (name, message) = if (quest!!.hasCountDownTimer) {
            addMarkDoneAction(questId, notificationBuilder)
            Pair(
                "Quest complete",
                "${quest.name} is all done!"
            )
        } else {
            val timeRange = quest.timeRanges.last()
            if (timeRange.type == TimeRange.Type.POMODORO_WORK) {
                addStartBreakAction(questId, notificationBuilder)
                Pair(
                    "Pomodoro complete",
                    "Your pomodoro is added to ${quest.name}. Ready for a break?"
                )
            } else {
                if (quest.hasCompletedAllTimeRanges()) {
                    addMarkDoneAction(questId, notificationBuilder)
                    Pair(
                        "Break complete",
                        "${quest.name} is all done!"
                    )
                } else {
                    addStartWorkAction(questId, notificationBuilder)
                    Pair(
                        "Break complete",
                        "Ready to work on ${quest.name}?"
                    )
                }
            }
        }

        notificationBuilder.setContentIntent(createContentIntent(questId))

        val notification = notificationBuilder
            .setContentTitle(name)
            .setContentText(message)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(Constants.QUEST_TIMER_NOTIFICATION_ID, notification)

        return Job.Result.SUCCESS
    }

    private fun addStartWorkAction(
        questId: String,
        notificationBuilder: NotificationCompat.Builder
    ) {
        val intent = Intent(context, CompleteQuestReceiver::class.java)
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId)

        notificationBuilder.addAction(
            R.drawable.ic_target_black_24dp,
            "Do it",
            getBroadcastPendingIntent(intent)
        )
    }

    private fun addStartBreakAction(
        questId: String,
        notificationBuilder: NotificationCompat.Builder
    ) {
        val intent = Intent(context, CompleteQuestReceiver::class.java)
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId)

        notificationBuilder.addAction(
            R.drawable.ic_flower_black_24dp,
            "Take Break",
            getBroadcastPendingIntent(intent)
        )
    }

    private fun createContentIntent(questId: String): PendingIntent {

        return PendingIntent.getActivity(
            context,
            Random().nextInt(),
            IntentUtil.showTimer(questId, context),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun addMarkDoneAction(
        questId: String,
        notificationBuilder: NotificationCompat.Builder
    ) {
        val intent = Intent(context, CompleteQuestReceiver::class.java)
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId)

        notificationBuilder.addAction(
            R.drawable.ic_done_black_24dp,
            "Mark Done",
            getBroadcastPendingIntent(intent)
        )
    }

    private fun getBroadcastPendingIntent(
        intent: Intent
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            Random().nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    companion object {
        const val TAG = "job_timer_complete_notification_tag"
    }
}

interface TimerCompleteScheduler {
    fun schedule(questId: String, after: Duration<Second>)
    fun cancelAll()
}

class AndroidJobTimerCompleteScheduler : TimerCompleteScheduler {

    override fun schedule(questId: String, after: Duration<Second>) {
        val bundle = PersistableBundleCompat()
        bundle.putString("questId", questId)
        JobRequest.Builder(TimerCompleteNotificationJob.TAG)
            .setUpdateCurrent(true)
            .setExtras(bundle)
            .setExact(after.millisValue)
            .build()
            .schedule()
    }

    override fun cancelAll() {
        JobManager.instance().cancelAllForTag(TimerCompleteNotificationJob.TAG)
    }
}