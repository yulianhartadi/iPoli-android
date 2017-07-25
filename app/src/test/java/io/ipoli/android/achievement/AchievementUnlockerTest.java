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
    public void findUnlocked_progressWith1CompletedQuest_unlockFirstQuestCompleted() {
        AchievementsProgress progress = new AchievementsProgress(new ActionCountPerDay(0, LocalDate.now()));
        progress.incrementCompletedQuestCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_QUEST_COMPLETED));
    }

    @Test
    public void findUnlocked_progressWith10CompletedQuest_unlockComplete10QuestsInADay() {
        AchievementsProgress progress = new AchievementsProgress(new ActionCountPerDay(9, LocalDate.now()));
        progress.incrementCompletedQuestsInADay();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.COMPLETE_10_QUESTS_IN_A_DAY));
    }

    @Test
    public void findUnlocked_progressWithLessThan10CompletedQuest_doNotUnlockComplete10QuestsInADay() {
        AchievementsProgress progress = new AchievementsProgress(new ActionCountPerDay(9, LocalDate.now().minusDays(1)));
        progress.incrementCompletedQuestsInADay();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(0));
    }
}
