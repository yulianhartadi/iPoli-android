package io.ipoli.android.quest.receiver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import io.ipoli.android.Constants
import io.ipoli.android.common.AsyncBroadcastReceiver
import io.ipoli.android.common.notification.QuickDoNotificationUtil
import io.ipoli.android.common.view.AppWidgetUtil
import io.ipoli.android.quest.show.usecase.CompleteTimeRangeUseCase
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/10/2018.
 */
class CompleteQuestReceiver : AsyncBroadcastReceiver() {

    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }
    private val playerRepository by required { playerRepository }
    private val questRepository by required { questRepository }

    override suspend fun onReceiveAsync(context: Context, intent: Intent) {
        val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
        completeTimeRangeUseCase.execute(CompleteTimeRangeUseCase.Params(questId))

        val player = playerRepository.find()!!
        if (player.preferences.isQuickDoNotificationEnabled) {
            val todayQuests = questRepository.findScheduledAt(LocalDate.now())

            launch(UI) {
                QuickDoNotificationUtil.update(context, todayQuests, player)
                updateUIElements(context)
            }
        } else {
            launch(UI) {
                updateUIElements(context)
            }
        }
    }

    private fun updateUIElements(context: Context) {
        removeTimerNotification(context)
        AppWidgetUtil.updateAgendaWidget(context)
    }

    private fun removeTimerNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.QUEST_TIMER_NOTIFICATION_ID)
    }
}