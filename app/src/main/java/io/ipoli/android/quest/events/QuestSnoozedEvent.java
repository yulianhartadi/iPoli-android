package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 2/21/16.
 */
public class QuestSnoozedEvent {
    private Quest quest;

    public QuestSnoozedEvent(Quest quest) {
        this.quest = quest;
    }
}
