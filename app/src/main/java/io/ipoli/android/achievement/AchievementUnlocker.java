package io.ipoli.android.achievement;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementUnlocker {

    private final Map<Achievement, AchievementConstraint> achievementToConstraint = new HashMap<>();

    public AchievementUnlocker() {
        achievementToConstraint.put(Achievement.FIRST_QUEST_COMPLETED, new FirstQuestCompletedConstraint());
        achievementToConstraint.put(Achievement.COMPLETE_10_QUESTS_IN_A_DAY, new Complete10QuestInADayConstraint());
        achievementToConstraint.put(Achievement.GAIN_100_XP_IN_A_DAY, new Gain100XPInADayConstraint());
        achievementToConstraint.put(Achievement.COMPLETE_QUEST_FOR_100_DAYS_IN_A_ROW, new CompleteAQuestFor100DaysConstraint());
        achievementToConstraint.put(Achievement.LEVEL_15TH, new Level15ThConstraint());
        achievementToConstraint.put(Achievement.LEVEL_20TH, new Level20ThConstraint());
        achievementToConstraint.put(Achievement.COMPLETE_DAILY_CHALLENGE_FOR_5_DAYS_IN_A_ROW, new CompleteDailyChallengeFor5DaysConstraint());
    }

    @NonNull
    public List<Achievement> findUnlocked(Set<Integer> unlockedAchievementCodes, AchievementsProgress progress) {
        List<Achievement> achievementsToUnlock = new ArrayList<>();
        for (Achievement achievement : Achievement.values()) {
            if (unlockedAchievementCodes.contains(achievement.code)) {
                continue;
            }
            addAchievementIfUnlocked(progress, achievementsToUnlock, achievement);
        }
        return achievementsToUnlock;
    }

    private void addAchievementIfUnlocked(AchievementsProgress progress, List<Achievement> achievementsToUnlock, Achievement achievement) {
        if (achievementToConstraint.get(achievement).shouldUnlock(progress)) {
            achievementsToUnlock.add(achievement);
        }
    }

    interface AchievementConstraint {
        boolean shouldUnlock(AchievementsProgress progress);
    }

    private class FirstQuestCompletedConstraint implements AchievementConstraint {

        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getCompletedQuestCount() == 1;
        }
    }

    private class Complete10QuestInADayConstraint implements AchievementConstraint {
        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getCompletedQuestsInADay().getCount() == 10;
        }
    }

    private class Gain100XPInADayConstraint implements AchievementConstraint {
        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getExperienceInADay().getCount() >= 100;
        }
    }

    private class CompleteAQuestFor100DaysConstraint implements AchievementConstraint {
        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getCompletedQuestsInARow().getCount() == 100;
        }
    }

    private abstract class LevelConstraint implements AchievementConstraint {

        private final int requiredLevel;

        LevelConstraint(int requiredLevel) {
            this.requiredLevel = requiredLevel;
        }

        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getPlayerLevel() >= requiredLevel;
        }
    }

    private class Level15ThConstraint extends LevelConstraint {

        Level15ThConstraint() {
            super(15);
        }
    }

    private class Level20ThConstraint extends LevelConstraint {

        Level20ThConstraint() {
            super(20);
        }
    }

    private class CompleteDailyChallengeFor5DaysConstraint implements AchievementConstraint {
        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getCompletedDailyChallengesInARow().getCount() >= 5;
        }
    }
}
