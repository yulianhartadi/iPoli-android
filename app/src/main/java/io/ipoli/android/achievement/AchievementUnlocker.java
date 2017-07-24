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

    public static final int ACTION_COMPLETE_QUEST = 1;
    public static final int ACTION_COMPLETE_DAILY_CHALLENGE = 2;
    public static final int ACTION_COMPLETE_CHALLENGE = 3;

    private final Map<Achievement, AchievementChecker> achievementToChecker = new HashMap<>();

    public AchievementUnlocker() {
        achievementToChecker.put(Achievement.FIRST_QUEST_COMPLETED, new FirstQuestCompletedChecker());
    }

    public List<Achievement> checkForNewAchievement(int action, Set<Integer> unlockedAchievementCodes, AchievementsProgress progress) {
        updateProgress(action, progress);
        return findAchievementsToUnlock(unlockedAchievementCodes, progress);
    }

    protected void updateProgress(int action, AchievementsProgress progress) {
        switch (action) {
            case ACTION_COMPLETE_QUEST:
                progress.incrementQuestCompleteCount();
                break;
        }
    }

    @NonNull
    protected List<Achievement> findAchievementsToUnlock(Set<Integer> unlockedAchievementCodes, AchievementsProgress progress) {
        List<Achievement> achievementsToUnlock = new ArrayList<>();
        for (Achievement achievement : Achievement.values()) {
            if (unlockedAchievementCodes.contains(achievement.code)) {
                continue;
            }
            addAchievementIfUnlocked(progress, achievementsToUnlock, achievement);
        }
        return achievementsToUnlock;
    }

    protected void addAchievementIfUnlocked(AchievementsProgress progress, List<Achievement> achievementsToUnlock, Achievement achievement) {
        if (achievementToChecker.get(achievement).shouldUnlock(progress)) {
            achievementsToUnlock.add(achievement);
        }
    }

    interface AchievementChecker {
        boolean shouldUnlock(AchievementsProgress progress);
    }

    class FirstQuestCompletedChecker implements AchievementChecker {

        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getCompleteQuestCount() == 1;
        }
    }
}
