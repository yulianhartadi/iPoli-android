package io.ipoli.android.quest.widget.agenda

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import io.ipoli.android.Constants
import io.ipoli.android.MyPoliApp
import io.ipoli.android.R
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.di.BackgroundModule
import io.ipoli.android.common.text.CalendarFormatter
import io.ipoli.android.quest.receiver.CompleteQuestReceiver
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import java.util.*


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/10/2018.
 */
class AgendaWidgetProvider : AppWidgetProvider(), Injects<BackgroundModule> {

    private val playerRepository by required { playerRepository }

    companion object {
        const val WIDGET_QUEST_LIST_ACTION =
            "mypoli.android.intent.actions.WIDGET_QUEST_LIST_ACTION"

        const val QUEST_ACTION_EXTRA_KEY = "quest_action"

        const val QUEST_ACTION_VIEW = 1
        const val QUEST_ACTION_COMPLETE = 2
    }

    override fun onReceive(context: Context, intent: Intent) {

        if (WIDGET_QUEST_LIST_ACTION == intent.action) {
            val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)

            val questAction = intent.getIntExtra(QUEST_ACTION_EXTRA_KEY, 0)

            if (questAction == QUEST_ACTION_VIEW) {
                context.startActivity(IntentUtil.showTimer(questId, context))

            } else if (questAction == QUEST_ACTION_COMPLETE) {
                onCompleteQuest(context, questId)

            } else throw IllegalArgumentException("Unknown agenda widget quest list action $questAction")
        }

        super.onReceive(context, intent)
    }

    private fun onCompleteQuest(context: Context, questId: String) {
        val i = Intent(context, CompleteQuestReceiver::class.java)
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId)
        context.sendBroadcast(i)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        inject(MyPoliApp.backgroundModule(context))
        val calendarFormatter = CalendarFormatter(MyPoliApp.instance)

        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek.getDisplayName(
            TextStyle.FULL, Locale.getDefault()
        )
        val date = calendarFormatter.dateWithoutYear(today)

        appWidgetIds.forEach {
            GlobalScope.launch(Dispatchers.IO) {
                val player = playerRepository.find()

                withContext(Dispatchers.Main) {

                    val rv = RemoteViews(context.packageName, R.layout.widget_agenda)

                    rv.setTextViewText(R.id.widgetDayOfWeek, dayOfWeek)
                    rv.setTextViewText(R.id.widgetDate, date)

                    rv.setOnClickPendingIntent(
                        R.id.widgetAgendaHeader,
                        createStartAppIntent(context)
                    )
                    rv.setOnClickPendingIntent(R.id.widgetAgendaPet, createShowPetIntent(context))
                    rv.setOnClickPendingIntent(R.id.widgetAgendaAdd, createQuickAddIntent(context))

                    if(player != null && player.isDead) {
                        rv.setViewVisibility(R.id.widgetAgendaPlayerDiedContainer, View.VISIBLE)
                        rv.setViewVisibility(R.id.widgetAgendaList, View.GONE)
                        rv.setViewVisibility(R.id.widgetAgendaEmpty, View.GONE)
                        rv.setViewVisibility(R.id.widgetAgendaPet, View.GONE)
                        rv.setViewVisibility(R.id.widgetAgendaAdd, View.GONE)

                        rv.setOnClickPendingIntent(
                            R.id.widgetAgendaRevive,
                            createStartAppIntent(context)
                        )
                    } else {

                        rv.setViewVisibility(R.id.widgetAgendaPlayerDiedContainer, View.GONE)
                        rv.setViewVisibility(R.id.widgetAgendaList, View.VISIBLE)
                        rv.setViewVisibility(R.id.widgetAgendaEmpty, View.VISIBLE)
                        rv.setViewVisibility(R.id.widgetAgendaPet, View.VISIBLE)
                        rv.setViewVisibility(R.id.widgetAgendaAdd, View.VISIBLE)

                        rv.setRemoteAdapter(
                            R.id.widgetAgendaList,
                            createQuestListIntent(context, it)
                        )

                        rv.setPendingIntentTemplate(
                            R.id.widgetAgendaList,
                            createQuestClickIntent(context, it)
                        )

                        rv.setEmptyView(R.id.widgetAgendaList, R.id.widgetAgendaEmpty)
                    }

                    appWidgetManager.notifyAppWidgetViewDataChanged(it, R.id.widgetDayOfWeek)
                    appWidgetManager.notifyAppWidgetViewDataChanged(it, R.id.widgetDate)

                    appWidgetManager.notifyAppWidgetViewDataChanged(it, R.id.widgetAgendaList)
                    appWidgetManager.updateAppWidget(it, rv)
                }
            }
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)

    }

    private fun createQuickAddIntent(context: Context) =
        IntentUtil.getActivityPendingIntent(context, IntentUtil.showQuickAdd(context))

    private fun createShowPetIntent(context: Context) =
        IntentUtil.getActivityPendingIntent(context, IntentUtil.showPet(context))

    private fun createQuestListIntent(context: Context, widgetId: Int) =
        Intent(context, AgendaWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }

    private fun createQuestClickIntent(context: Context, widgetId: Int): PendingIntent {
        val intent = Intent(context, AgendaWidgetProvider::class.java)
        intent.action = AgendaWidgetProvider.WIDGET_QUEST_LIST_ACTION
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createStartAppIntent(context: Context) =
        IntentUtil.getActivityPendingIntent(
            context,
            IntentUtil.startApp(context)
        )
}