package io.ipoli.android.challenge.events;

import io.ipoli.android.quest.data.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/27/16.
 */
public class NewChallengeCategoryChangedEvent {
    public final Category category;

    public NewChallengeCategoryChangedEvent(Category category) {
        this.category = category;
    }
}
