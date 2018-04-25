package io.ipoli.android.common

import android.content.Context
import android.content.Intent
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
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }

    fun showTimer(questId: String, context: Context) =
        Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SHOW_TIMER
            putExtra(Constants.QUEST_ID_EXTRA_KEY, questId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }

    fun startApp(context: Context) =
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
}