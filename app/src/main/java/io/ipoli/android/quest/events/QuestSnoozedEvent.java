package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/21/16.
 */
public class QuestSnoozedEvent {
    public final Quest quest;

    public QuestSnoozedEvent(Quest quest) {
        this.quest = quest;
    }
}
