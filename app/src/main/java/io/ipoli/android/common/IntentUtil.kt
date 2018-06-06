package io.ipoli.android.common

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import io.ipoli.android.Constants
import io.ipoli.android.MainActivity

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/16/2018.
 */
object IntentUtil {

    fun showQuickAdd(context: Context) =
        Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SHOW_QUICK_ADD
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

    fun showPet(context: Context) =
        Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SHOW_PET
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

    fun showTimer(questId: String, context: Context) =
        Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SHOW_TIMER
            putExtra(Constants.QUEST_ID_EXTRA_KEY, questId)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

    fun startPlanDay(context: Context) =
        Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_PLAN_DAY
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

    fun startApp(context: Context) =
        Intent(context, MainActivity::class.java).apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

    fun startRatePage(context: Context): Intent {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        return Intent(android.content.Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getActivityPendingIntent(context: Context, intent: Intent): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun getBroadcastPendingIntent(
        context: Context,
        intent: Intent
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
}