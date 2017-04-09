package io.ipoli.android.quest.events;

import org.threeten.bp.LocalDate;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/12/16.
 */
public class DuplicateQuestRequestEvent {
    public final Quest quest;
    public final LocalDate date;
    public final EventSource source;

    public DuplicateQuestRequestEvent(Quest quest, LocalDate date, EventSource source) {
        this.quest = quest;
        this.date = date;
        this.source = source;
    }
}
