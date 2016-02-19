package io.ipoli.android.quest.events;

import io.ipoli.android.quest.ui.QuestCalendarEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class QuestAddedToCalendarEvent {
    public final QuestCalendarEvent questCalendarEvent;

    public QuestAddedToCalendarEvent(QuestCalendarEvent questCalendarEvent) {
        this.questCalendarEvent = questCalendarEvent;
    }
}
