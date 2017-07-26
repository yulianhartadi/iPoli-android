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
    public void findUnlocked_100ExperienceInADay_unlock100ExperienceInADay() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setExperienceInADay(new ActionCountPerDay(90, LocalDate.now()));
        progress.incrementExperienceInADay(10);
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.GAIN_100_XP_IN_A_DAY));
    }

    @Test
    public void findUnlocked_lessThan100ExperienceInADay_doNotUnlock100ExperienceInADay() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setExperienceInADay(new ActionCountPerDay(90, LocalDate.now().minusDays(1)));
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
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_CHALLENGE_ACCEPTED));
    }
}