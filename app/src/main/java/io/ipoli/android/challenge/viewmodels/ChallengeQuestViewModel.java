package io.ipoli.android.challenge.viewmodels;

import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.BaseQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/13/16.
 */
public class ChallengeQuestViewModel {

    private final BaseQuest baseQuest;
    private boolean isRepeating;

    public ChallengeQuestViewModel(BaseQuest baseQuest, boolean isRepeating) {
        this.baseQuest = baseQuest;
        this.isRepeating = isRepeating;
    }

    public BaseQuest getBaseQuest() {
        return baseQuest;
    }

    public String getName() {
        return baseQuest.getName();
    }

    public Category getCategory() {
        return Category.valueOf(baseQuest.getCategory());
    }

    public boolean isRepeating() {
        return isRepeating;
    }

}
