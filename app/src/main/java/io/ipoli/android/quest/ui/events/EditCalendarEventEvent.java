package io.ipoli.android.quest.ui.events;

import android.view.View;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/22/16.
 */
public class EditCalendarEventEvent {
    public final View calendarEventView;
    public final Quest quest;

    public EditCalendarEventEvent(View calendarEventView, Quest quest) {
        this.calendarEventView = calendarEventView;
        this.quest = quest;
    }
}
