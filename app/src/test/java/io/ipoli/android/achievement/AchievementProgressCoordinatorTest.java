package io.ipoli.android.achievement;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.achievement.actions.CompleteQuestAction;
import io.ipoli.android.quest.data.Quest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class AchievementProgressCoordinatorTest {

    private AchievementsProgress progress;

    @Before
    public void beforeEachTest() {
        progress = AchievementsProgress.create();
    }

    @Test
    public void update_completeQuest_completedQuestCountIs1() {
        Quest quest = new Quest("Welcome");
        quest.setExperience(20L);
        AchievementsProgressCoordinator.update(new CompleteQuestAction(quest), progress);
        assertThat(progress.getCompletedQuestCount(), is(1));
    }

    @Test
    public void update_completeQuest_completedQuestsInADayIs1() {
        Quest quest = new Quest("Welcome");
        quest.setExperience(20L);
        AchievementsProgressCoordinator.update(new CompleteQuestAction(quest), progress);
        assertThat(progress.getCompletedQuestsInADay().getCount(), is(1));
    }
}
