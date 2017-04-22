package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */

public class WorkConstraint {
    public boolean shouldApply(Quest quest) {
        return quest.getCategoryType() == Category.WORK;
    }
}
