package io.ipoli.android.achievement;

import io.ipoli.android.achievement.actions.AchievementAction;
import io.ipoli.android.achievement.actions.CompleteQuestAction;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/24/17.
 */
public class AchievementsProgressCoordinator {

    public static void update(AchievementAction action, AchievementsProgress progress) {
        switch (action.getAction()) {
            case COMPLETE_QUEST:
                CompleteQuestAction completeQuestAction = (CompleteQuestAction) action;
                progress.incrementCompletedQuestCount();
                progress.incrementCompletedQuestsInADay();
                progress.incrementExperienceInADay(completeQuestAction.getQuest().getExperience().intValue());
                progress.incrementCompletedQuestsInARow();
                break;
        }
    }
}
