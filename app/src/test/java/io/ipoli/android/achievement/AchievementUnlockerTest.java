package io.ipoli.android.achievement;

import org.junit.BeforeClass;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementUnlockerTest {

    private static AchievementUnlocker unlocker;

    @BeforeClass
    public static void setUp() {
        unlocker = new AchievementUnlocker();
    }

    @Test
    public void findUnlocked_1CompletedQuest_unlockFirstQuestCompleted() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementCompletedQuestCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_QUEST_COMPLETED));
    }

    @Test
    public void findUnlocked_10CompletedQuests_unlockComplete10QuestsInADay() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedQuestsInADay(new ActionCountPerDay(9, LocalDate.now()));
        progress.incrementCompletedQuestsInADay();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.COMPLETE_10_QUESTS_IN_A_DAY));
    }

    @Test
    public void findUnlocked_lessThan10CompletedQuests_doNotUnlockComplete10QuestsInADay() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedQuestsInADay(new ActionCountPerDay(9, LocalDate.now().minusDays(1)));
        progress.incrementCompletedQuestsInADay();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }

    @Test
    public void findUnlocked_500ExperienceInADay_unlock500ExperienceInADay() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setExperienceInADay(new ActionCountPerDay(490, LocalDate.now()));
        progress.incrementExperienceInADay(10);
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.GAIN_500_XP_IN_A_DAY));
    }

    @Test
    public void findUnlocked_lessThan500ExperienceInADay_doNotUnlock500ExperienceInADay() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setExperienceInADay(new ActionCountPerDay(490, LocalDate.now().minusDays(1)));
        progress.incrementExperienceInADay(10);
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }

    @Test
    public void findUnlocked_100CompletedQuestsInARow_unlockCompleteAQuest100DaysInARow() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedQuestsInARow(new ActionCountPerDay(99, LocalDate.now().minusDays(1)));
        progress.incrementCompletedQuestsInARow();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.COMPLETE_QUEST_FOR_100_DAYS_IN_A_ROW));
    }

    @Test
    public void findUnlocked_lessThanCompletedQuestsInARow_doNotUnlockCompleteAQuest100DaysInARow() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedQuestsInARow(new ActionCountPerDay(90, LocalDate.now().minusDays(2)));
        progress.incrementCompletedQuestsInARow();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }

    @Test
    public void findUnlocked_levelUpTo15th_unlock15thLevelAchievement() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setPlayerLevel(15);
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.LEVEL_15TH));
    }

    @Test
    public void findUnlocked_5CompletedDailyChallengesInARow_unlockCompleteDailyChallenges5DaysInARow() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedDailyChallengesInARow(new ActionCountPerDay(4, LocalDate.now().minusDays(1)));
        progress.incrementCompletedDailyChallengesInARow();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.COMPLETE_DAILY_CHALLENGE_FOR_5_DAYS_IN_A_ROW));
    }

    @Test
    public void findUnlocked_lessThan5CompletedDailyChallengesInARow_doNotUnlockCompleteDailyChallenges5DaysInARow() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedQuestsInARow(new ActionCountPerDay(4, LocalDate.now().minusDays(2)));
        progress.incrementCompletedDailyChallengesInARow();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }

    @Test
    public void findUnlocked_sentFeedback_unlockFeedbackSent() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementFeedbackSent();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FEEDBACK_SENT));
    }

    @Test
    public void findUnlocked_createRepeatingQuest_unlockFirstRepeatingQuestCreated() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementRepeatingQuestCreatedCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_REPEATING_QUEST_CREATED));
    }

    @Test
    public void findUnlocked_acceptChallenge_unlockFirstChallengeAccepted() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementChallengeAcceptedCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_CHALLENGE_CREATED));
    }

    @Test
    public void findUnlocked_completeDailyChallenge_unlockFirstDailyChallengeCompleted() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementCompletedDailyChallengeCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_DAILY_CHALLENGE_COMPLETED));
    }

    @Test
    public void findUnlocked_create5Posts_unlockFirstPostAnd5PostsCreated() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setPostAddedCount(4);
        progress.incrementPostAddedCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(2));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_POST_CREATED));
        assertTrue(unlockedAchievements.contains(Achievement.FIVE_POSTS_CREATED));
    }

    @Test
    public void findUnlocked_changeAvatar_unlockFirstAvatarChanged() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementAvatarChangedCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_AVATAR_CHANGED));
    }

    @Test
    public void findUnlocked_useReward_unlockFirstRewardUsed() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementRewardUsedCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_REWARD_USED));
    }

    @Test
    public void findUnlocked_enablePowerUp_unlockFirsPowerUpEnabled() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementPowerUpBoughtCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_POWER_UP));
    }

    @Test
    public void findUnlocked_collect1KCoins_unlockHave1KCoins() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setLifeCoinCount(1000L);
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.HAVE_1K_COINS));
    }

    @Test
    public void findUnlocked_collectLessThan1KCoins_doNotUnlockHave1KCoins() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setLifeCoinCount(900L);
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }

    @Test
    public void findUnlocked_inviteFriend_unlockFirstFriendInvited() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementInvitedFriendCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.INVITE_FRIEND));
    }

    @Test
    public void findUnlocked_changePet_unlockFirstPetChanged() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementPetChangedCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.CHANGE_PET));
    }

    @Test
    public void findUnlocked_completeChallenge_unlockFirstChallengeCompleted() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementCompletedChallengesCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_CHALLENGE_COMPLETED));
    }
}