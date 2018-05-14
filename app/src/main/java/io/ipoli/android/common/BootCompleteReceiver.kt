package io.ipoli.android.common

import android.content.Context
import android.content.Intent
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
        if (playerRepository.hasPlayer()) {
            reminderScheduler.schedule()
        }
    }

}