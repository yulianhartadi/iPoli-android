package io.ipoli.android.quest.calendar.ui.dayview

import io.ipoli.android.common.ui.Color

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
interface CalendarEvent {
    var duration: Int

    var startMinute: Int

    var name: String

    var backgroundColor: Color
}