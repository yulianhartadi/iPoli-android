package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.Constants;
import io.ipoli.android.app.scheduling.DailyScheduler;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.LearningConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

import static io.ipoli.android.app.utils.Time.h2Min;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */
public class LearningConstraintTest {

    private Constraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new LearningConstraint(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE, DailyScheduler.DEFAULT_TIME_SLOT_DURATION);
    }

    @Test
    public void shouldApplyToLearningQuest() {
        Task t = new Task(0, Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT, TimePreference.ANY, Category.LEARNING);
        assertTrue(constraint.shouldApply(t));
    }

    @Test
    public void shouldNotApplyToNonLearningQuest() {
        Task t = new Task(0, Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT, TimePreference.ANY, Category.FUN);
        assertFalse(constraint.shouldApply(t));
    }

    @Test
    public void shouldHaveZeroProbabilityForFirstSlot() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE)), is(0.0));
    }

    @Test
    public void shouldHaveEqualProbabilitiesInFirstPeakEdges() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(h2Min(2))),
                is(dist.at(constraint.getSlotForMinute(h2Min(4) - 1))));
    }

    @Test
    public void shouldHaveHigherProbabilityInPeakThanSlope() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE + h2Min(3))),
                is(greaterThan(dist.at(constraint.getSlotForMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE + h2Min(4) + 15)))));
    }

    @Test
    public void shouldHaveHigherProbabilityInSlopeThanPlateau() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE + h2Min(4) + 15)),
                is(greaterThan(dist.at(constraint.getSlotForMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE + h2Min(6))))));
    }
}
