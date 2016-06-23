package io.ipoli.android.quest.events;

import io.ipoli.android.quest.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class NewQuestContextChangedEvent {
    public final Category category;

    public NewQuestContextChangedEvent(Category category) {
        this.category = category;
    }
}
