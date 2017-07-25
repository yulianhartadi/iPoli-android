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
        AchievementsProgress progress = AchievementsProgress.create();
        progress.incrementCompletedQuestCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_QUEST_COMPLETED));
    }

    @Test
    public void findUnlocked_10CompletedQuests_unlockComplete10QuestsInADay() {
        AchievementsProgress progress = AchievementsProgress.create();
        progress.setCompletedQuestsInADay(new ActionCountPerDay(9, LocalDate.now()));
        progress.incrementCompletedQuestsInADay();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.COMPLETE_10_QUESTS_IN_A_DAY));
    }

    @Test
    public void findUnlocked_lessThan10CompletedQuests_doNotUnlockComplete10QuestsInADay() {
        AchievementsProgress progress = AchievementsProgress.create();
        progress.setCompletedQuestsInADay(new ActionCountPerDay(9, LocalDate.now().minusDays(1)));
        progress.incrementCompletedQuestsInADay();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }

    @Test
    public void findUnlocked_100ExperienceInADay_unlock100ExperienceInADay() {
        AchievementsProgress progress = AchievementsProgress.create();
        progress.setExperienceInADay(new ActionCountPerDay(90, LocalDate.now()));
        progress.incrementExperienceInADay(10);
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.GAIN_100_XP_IN_A_DAY));
    }

    @Test
    public void findUnlocked_lessThan100ExperienceInADay_doNotUnlock100ExperienceInADay() {
        AchievementsProgress progress = AchievementsProgress.create();
        progress.setExperienceInADay(new ActionCountPerDay(90, LocalDate.now().minusDays(1)));
        progress.incrementExperienceInADay(10);
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }

    @Test
    public void findUnlocked_100CompletedAQuestsInARow_unlockCompleteAQuest100DaysInARow() {
        AchievementsProgress progress = AchievementsProgress.create();
        progress.setCompletedQuestsInARow(new ActionCountPerDay(99, LocalDate.now().minusDays(1)));
        progress.incrementCompletedQuestsInARow();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.COMPLETE_QUEST_FOR_100_DAYS_IN_A_ROW));
    }

    @Test
    public void findUnlocked_lessThanCompletedAQuestsInARow_doNotUnlockCompleteAQuest100DaysInARow() {
        AchievementsProgress progress = AchievementsProgress.create();
        progress.setCompletedQuestsInARow(new ActionCountPerDay(90, LocalDate.now().minusDays(2)));
        progress.incrementCompletedQuestsInARow();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }
}