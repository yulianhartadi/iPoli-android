package io.ipoli.android.common

import android.content.Context
import android.content.Intent
import io.ipoli.android.R.string.quests
import io.ipoli.android.common.notification.QuickDoNotificationUtil
import io.ipoli.android.common.view.AppWidgetUtil
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 06/30/2018.
 */
class DateChangedReceiver : AsyncBroadcastReceiver() {

    private val playerRepository by required { playerRepository }
    private val questRepository by required { questRepository }

    override suspend fun onReceiveAsync(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_DATE_CHANGED) {
            val p = playerRepository.find()
            if (p != null) {
                launch(UI) {
                    AppWidgetUtil.updateAgendaWidget(context)
                }
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