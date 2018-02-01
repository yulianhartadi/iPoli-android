package mypoli.android.quest.schedule.calendar.dayview.view.widget

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/2/17.
 */
interface CalendarEvent {

    val id: String

    val duration: Int

    val startMinute: Int
}