package io.ipoli.android.app.scheduling;

import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class PriorityEstimator {

    public int estimate(Quest quest) {
        int score = 0;
        int priority = quest.getPriority();
        switch (priority) {
            case Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY:
                score += 8;
                break;
            case Quest.PRIORITY_IMPORTANT_URGENT:
                score += 5;
                break;
            case Quest.PRIORITY_IMPORTANT_NOT_URGENT:
                score += 3;
                break;
            case Quest.PRIORITY_NOT_IMPORTANT_URGENT:
                score += 1;
                break;
        }
        if (quest.getStartTimePreference() != TimePreference.ANY) {
            score += 5;
        }
        Category category = quest.getCategoryType();
        if (category == Category.WORK) {
            score += 5;
        }
        if (category == Category.LEARNING || category == Category.WELLNESS) {
            score += 3;
        }
        if(quest.isFromChallenge()) {
            score += 3;
        }
        return score;
    }
}
