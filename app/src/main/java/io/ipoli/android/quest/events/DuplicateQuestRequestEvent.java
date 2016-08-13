package io.ipoli.android.quest.events;

import java.util.Date;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/12/16.
 */
public class DuplicateQuestRequestEvent {
    public final Quest quest;
    public final Date date;

    public DuplicateQuestRequestEvent(Quest quest) {
        this.quest = quest;
        this.date = null;
    }

    public DuplicateQuestRequestEvent(Quest quest, Date date) {
        this.quest = quest;
        this.date = date;
    }
}
