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
        achievementToConstraint.put(Achievement.FIRST_QUEST_COMPLETED, progress -> progress.getCompletedQuestCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_CHALLENGE_CREATED, progress -> progress.getCreatedChallengeCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_AVATAR_CHANGED, progress -> progress.getAvatarChangedCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_DAILY_CHALLENGE_COMPLETED, progress -> progress.getCompletedDailyChallengeCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_POST_CREATED, progress -> progress.getPostAddedCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_REPEATING_QUEST_CREATED, progress -> progress.getCreatedRepeatedQuestCount() >= 1);
        achievementToConstraint.put(Achievement.COMPLETE_10_QUESTS_IN_A_DAY, new Complete10QuestInADayConstraint());
        achievementToConstraint.put(Achievement.GAIN_100_XP_IN_A_DAY, new Gain100XPInADayConstraint());
        achievementToConstraint.put(Achievement.COMPLETE_QUEST_FOR_100_DAYS_IN_A_ROW, new CompleteAQuestFor100DaysConstraint());
        achievementToConstraint.put(Achievement.LEVEL_15TH, new PlayerLevelConstraint(15));
        achievementToConstraint.put(Achievement.LEVEL_20TH, new PlayerLevelConstraint(20));
        achievementToConstraint.put(Achievement.COMPLETE_DAILY_CHALLENGE_FOR_5_DAYS_IN_A_ROW, new CompleteDailyChallengeFor5DaysConstraint());
        achievementToConstraint.put(Achievement.FIVE_POSTS_CREATED, progress -> progress.getPostAddedCount() >= 5);
        achievementToConstraint.put(Achievement.HAVE_1K_COINS, progress -> progress.getLifeCoinCount() >= 1000);
        achievementToConstraint.put(Achievement.INVITE_FRIEND, progress -> progress.getInvitedFriendCount() >= 1);
        achievementToConstraint.put(Achievement.CHANGE_PET, progress -> progress.getPetChangeCount() >= 1);
        achievementToConstraint.put(Achievement.PET_DIED, progress -> progress.getPetDiedCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_FOLLOW, progress -> progress.getFollowCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_FOLLOWER, progress -> progress.getFollowerCount() >= 1);
        achievementToConstraint.put(Achievement.FEEDBACK_SENT, progress -> progress.getFeedbackSentCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_REWARD_USED, progress -> progress.getRewardUsedCount() >= 1);
        achievementToConstraint.put(Achievement.FIRST_POWER_UP, progress -> progress.getPowerUpCount() >= 1);
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
        if(!achievementToConstraint.containsKey(achievement)) {
            System.err.println(achievement.name());
        }
        if (achievementToConstraint.get(achievement).shouldUnlock(progress)) {
            achievementsToUnlock.add(achievement);
        }
    }

    interface AchievementConstraint {
        boolean shouldUnlock(AchievementsProgress progress);
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
            return progress.getCompletedQuestsInARow().getCount() >= 100;
        }
    }

    private class PlayerLevelConstraint implements AchievementConstraint {

        private final int requiredLevel;

        PlayerLevelConstraint(int requiredLevel) {
            this.requiredLevel = requiredLevel;
        }

        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getPlayerLevel() >= requiredLevel;
        }
    }

    private class CompleteDailyChallengeFor5DaysConstraint implements AchievementConstraint {
        @Override
        public boolean shouldUnlock(AchievementsProgress progress) {
            return progress.getCompletedDailyChallengesInARow().getCount() >= 5;
        }
    }
}
