package io.ipoli.android.quest.widget.agenda

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.AndroidIcon
import io.ipoli.android.myPoliApp
import io.ipoli.android.quest.Quest
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import java.util.concurrent.CopyOnWriteArrayList


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/10/2018.
 */
class AgendaWidgetViewsFactory(private val context: Context) :
    RemoteViewsService.RemoteViewsFactory, Injects<Module> {

    private var quests = CopyOnWriteArrayList<Quest>()

    private val questRepository by required { questRepository }

    override fun onCreate() {
        inject(myPoliApp.module(context))
    }

    override fun getLoadingView() = null

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onDataSetChanged() {
        quests.clear()
        quests.addAll(questRepository.findScheduledAt(LocalDate.now()).filter { !it.isCompleted })
    }

    override fun hasStableIds() = true

    override fun getViewAt(position: Int): RemoteViews {

        val rv = RemoteViews(context.packageName, R.layout.item_widget_agenda)
        val q = quests[position]

        rv.setTextViewText(R.id.widgetQuestName, q.name)
        rv.setTextViewText(R.id.widgetQuestStartTime, formatStartTime(q))

        val icon = q.icon?.let { AndroidIcon.valueOf(it.name).icon }
            ?: Ionicons.Icon.ion_android_clipboard

        val iconDrawable =
            IconicsDrawable(context)
                .icon(icon)
                .colorRes(R.color.md_white)
                .sizeDp(24)

        rv.setImageViewBitmap(R.id.widgetQuestIcon, iconDrawable.toBitmap())

        val iconBgColor = AndroidColor.valueOf(q.color.name).color500

        rv.setImageViewBitmap(R.id.widgetQuestIconBackground, createIconBackground(iconBgColor))

        rv.setOnClickFillInIntent(R.id.widgetSelectableBackground, createClickQuestIntent(q))

        val checkboxIcon =
            IconicsDrawable(context)
                .icon(Ionicons.Icon.ion_android_checkbox_outline_blank)
                .colorRes(R.color.md_light_text_70)
                .sizeDp(24)
        rv.setImageViewBitmap(R.id.widgetQuestComplete, checkboxIcon.toBitmap())
        rv.setOnClickFillInIntent(R.id.widgetQuestComplete, createCompleteQuestIntent(q))

        return rv
    }

    private fun createClickQuestIntent(q: Quest): Intent {
        val b = Bundle().apply {
            putInt(
                AgendaWidgetProvider.QUEST_ACTION_EXTRA_KEY,
                AgendaWidgetProvider.QUEST_ACTION_VIEW
            )
            putString(Constants.QUEST_ID_EXTRA_KEY, q.id)
        }

        return Intent().putExtras(b)
    }

    private fun createCompleteQuestIntent(q: Quest): Intent {
        val b = Bundle().apply {
            putInt(
                AgendaWidgetProvider.QUEST_ACTION_EXTRA_KEY,
                AgendaWidgetProvider.QUEST_ACTION_COMPLETE
            )
            putString(Constants.QUEST_ID_EXTRA_KEY, q.id)
        }

        return Intent().putExtras(b)
    }

    private fun createIconBackground(iconBgColor: Int): Bitmap? {
        val drawable =
            context.getDrawable(R.drawable.widget_agenda_item_icon_background) as GradientDrawable
        drawable.setColor(ContextCompat.getColor(context, iconBgColor))

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun formatStartTime(quest: Quest): String {
        val start = quest.startTime ?: return "Unscheduled"
        val end = start.plus(quest.actualDuration.asMinutes.intValue)
        return "$start - $end"
    }

    override fun getCount() = quests.size

    override fun getViewTypeCount() = 1

    override fun onDestroy() {
    }

}