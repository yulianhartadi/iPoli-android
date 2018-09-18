package io.ipoli.android.common

import android.content.Context
import android.content.Intent
import io.ipoli.android.common.notification.QuickDoNotificationUtil
import io.ipoli.android.common.view.AppWidgetUtil
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/03/2018.
 */
class BootCompleteReceiver : AsyncBroadcastReceiver() {

    private val playerRepository by required { playerRepository }
    private val questRepository by required { questRepository }
    private val reminderScheduler by required { reminderScheduler }

    override suspend fun onReceiveAsync(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val p = playerRepository.find()
            if (p != null) {
                reminderScheduler.schedule()
                launch(UI) {
                    AppWidgetUtil.updateAgendaWidget(context)
                    AppWidgetUtil.updateHabitWidget(context)
                    if (p.isDead) {
                        QuickDoNotificationUtil.showDefeated(context)
                    } else {
                        val quests = questRepository.findScheduledAt(LocalDate.now())
                        if (p.preferences.isQuickDoNotificationEnabled) {
                            QuickDoNotificationUtil.update(
                                context = context,
                                quests = quests
                            )
                        }
                    }
                }
            }
        }
    }

}