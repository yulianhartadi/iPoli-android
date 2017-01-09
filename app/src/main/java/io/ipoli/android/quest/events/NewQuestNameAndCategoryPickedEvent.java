package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/17.
 */
public class NewQuestNameAndCategoryPickedEvent {
    public final String name;
    public final Category category;

    public NewQuestNameAndCategoryPickedEvent(String name, Category category) {
        this.name = name;
        this.category = category;
    }
}
