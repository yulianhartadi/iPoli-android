package io.ipoli.android.quest.events;

import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class QuestContextUpdatedEvent {
    public final Quest quest;
    public final Category category;

    public QuestContextUpdatedEvent(Quest quest, Category category) {
        this.quest = quest;
        this.category = category;
    }
}
