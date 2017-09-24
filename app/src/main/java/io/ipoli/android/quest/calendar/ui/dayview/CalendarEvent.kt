package io.ipoli.android.quest.calendar.ui.dayview

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
interface CalendarEvent {
    var duration: Int
        get

    var startMinute: Int
        get

    var name: String
        get

    var color: Int
        get
}