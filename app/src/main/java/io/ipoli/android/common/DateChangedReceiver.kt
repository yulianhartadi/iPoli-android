package io.ipoli.android.common

import android.content.Context
import android.content.Intent
import io.ipoli.android.common.view.AppWidgetUtil
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
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
                GlobalScope.launch(Dispatchers.Main) {
                    AppWidgetUtil.updateAgendaWidget(context)
                }
            }
        }
    }

}