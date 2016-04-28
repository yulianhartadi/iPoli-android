package io.ipoli.android.quest.events;

import io.ipoli.android.quest.viewmodels.QuestCalendarViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class QuestAddedToCalendarEvent {
    public final QuestCalendarViewModel questCalendarViewModel;

    public QuestAddedToCalendarEvent(QuestCalendarViewModel questCalendarViewModel) {
        this.questCalendarViewModel = questCalendarViewModel;
    }
}
