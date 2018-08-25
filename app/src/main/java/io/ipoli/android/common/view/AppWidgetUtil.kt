package io.ipoli.android.common.view

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import io.ipoli.android.habit.widget.HabitWidgetProvider
import io.ipoli.android.quest.widget.agenda.AgendaWidgetProvider

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/15/2018.
 */
object AppWidgetUtil {

    fun updateAgendaWidget(context: Context) {
        val widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, AgendaWidgetProvider::class.java)
        )
        if (widgetIds.isNotEmpty()) {
            Intent(context, AgendaWidgetProvider::class.java).apply {
                this.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                context.sendBroadcast(this)
            }
        }
    }

    fun updateHabitWidget(context: Context) {
        val widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, HabitWidgetProvider::class.java)
        )
        if (widgetIds.isNotEmpty()) {
            Intent(context, HabitWidgetProvider::class.java).apply {
                this.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                context.sendBroadcast(this)
            }
        }
    }
}