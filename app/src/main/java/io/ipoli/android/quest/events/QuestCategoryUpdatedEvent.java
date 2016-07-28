package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class QuestCategoryUpdatedEvent {
    public final Quest quest;
    public final Category category;

    public QuestCategoryUpdatedEvent(Quest quest, Category category) {
        this.quest = quest;
        this.category = category;
    }
}
