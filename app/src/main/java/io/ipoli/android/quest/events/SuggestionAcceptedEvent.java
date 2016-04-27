package io.ipoli.android.quest.events;

import io.ipoli.android.quest.viewmodels.QuestCalendarViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/27/16.
 */
public class SuggestionAcceptedEvent {
    public final QuestCalendarViewModel calendarEvent;

    public SuggestionAcceptedEvent(QuestCalendarViewModel calendarEvent) {
        this.calendarEvent = calendarEvent;
    }
}
