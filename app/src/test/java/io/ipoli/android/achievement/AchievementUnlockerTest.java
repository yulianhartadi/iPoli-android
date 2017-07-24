package io.ipoli.android.achievement;

import org.junit.BeforeClass;
import org.junit.Test;

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
        AchievementsProgress progress = new AchievementsProgress();
        progress.incrementQuestCompleteCount();
        List<Achievement> unlockedAchievements = unlocker.findUnlocked(new HashSet<>(), progress);
        assertThat(unlockedAchievements.size(), is(1));
        assertTrue(unlockedAchievements.contains(Achievement.FIRST_QUEST_COMPLETED));
    }
}
