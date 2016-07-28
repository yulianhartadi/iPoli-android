package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class NewQuestCategoryChangedEvent {
    public final Category category;

    public NewQuestCategoryChangedEvent(Category category) {
        this.category = category;
    }
}
