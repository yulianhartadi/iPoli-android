package io.ipoli.android.achievement;

import io.ipoli.android.achievement.actions.AchievementAction;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/24/17.
 */
public class AchievementsProgressUpdater {

    public static void update(AchievementAction action, AchievementsProgress progress) {
        switch (action.getAction()) {
            case COMPLETE_QUEST:
                progress.incrementCompletedQuestCount();
                progress.incrementCompletedQuestsInADay();
                break;
        }
    }
}
