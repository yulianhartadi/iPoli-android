package io.ipoli.android.quest.job

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.ipoli.android.MyPoliApp
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.datetime.toMillis
import io.ipoli.android.common.di.BackgroundModule
import io.ipoli.android.quest.receiver.ReminderReceiver
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDateTime
import space.traversal.kapsule.Kapsule

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/26/17.
 */

interface ReminderScheduler {
    fun schedule()
}

class AndroidJobReminderScheduler(private val context: Context) : ReminderScheduler {
    override fun schedule() {

        GlobalScope.launch(Dispatchers.IO) {

            val kap = Kapsule<BackgroundModule>()
            val entityReminderRepository by kap.required { entityReminderRepository }
            kap.inject(MyPoliApp.backgroundModule(context))

            val remindAt = entityReminderRepository.findNextReminderTime()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (remindAt == null) {
                alarmManager.cancel(createOperationIntent(context))
                return@launch
            }

            val show = IntentUtil.getActivityPendingIntent(context, IntentUtil.startApp(context))

            val clockInfo = AlarmManager.AlarmClockInfo(remindAt.toMillis(), show)

            alarmManager.setAlarmClock(
                clockInfo,
                createOperationIntent(context, remindAt)
            )
        }

    }

    private fun createOperationIntent(
        context: Context,
        remindAt: LocalDateTime? = null
    ) =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java).apply {
                remindAt?.let {
                    putExtra("remindAtUTC", it.toMillis())
                }
                action = ReminderReceiver.ACTION_SHOW_REMINDER
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
}