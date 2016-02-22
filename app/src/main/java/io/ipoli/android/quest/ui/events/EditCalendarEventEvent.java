package io.ipoli.android.quest.ui.events;

import android.view.View;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/22/16.
 */
public class EditCalendarEventEvent {
    public final View calendarEventView;

    public EditCalendarEventEvent(View calendarEventView) {
        this.calendarEventView = calendarEventView;
    }
}
