package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/17.
 */
public class NameAndCategoryPickedEvent {
    public final String name;
    public final Category category;

    public NameAndCategoryPickedEvent(String name, Category category) {
        this.name = name;
        this.category = category;
    }
}
