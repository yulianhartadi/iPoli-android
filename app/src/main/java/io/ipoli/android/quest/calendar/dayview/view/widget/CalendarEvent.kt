package io.ipoli.android.quest.calendar.dayview.view.widget

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */
interface CalendarEvent {

    val id: String

    val duration: Int

    val startMinute: Int

    val name: String
}