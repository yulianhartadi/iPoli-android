package mypoli.android.common

import android.content.Context
import android.content.Intent
import mypoli.android.Constants
import mypoli.android.MainActivity

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/16/2018.
 */
object IntentUtil {

    fun showTimer(questId: String, context: Context) =
        Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SHOW_TIMER
            putExtra(Constants.QUEST_ID_EXTRA_KEY, questId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

    fun startApp(context: Context) =
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
}