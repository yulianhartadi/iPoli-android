package io.ipoli.android.achievement;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/24/17.
 */
public class AchievementsProgressUpdater {

    public static void update(AchievementAction action, AchievementsProgress progress) {
        switch (action) {
            case COMPLETE_QUEST:
                progress.incrementCompletedQuestCount();
                break;
        }
    }
}
