package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.constraints.LearningConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
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
    private LearningConstraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new LearningConstraint();
    }

    @Test
    public void shouldApplyToLearningQuest() {
        Quest q = new Quest("q1", Category.LEARNING);
        assertTrue(constraint.shouldApply(q));
    }

    @Test
    public void shouldNotApplyToNonLearningQuest() {
        Quest q = new Quest("q1", Category.FUN);
        assertFalse(constraint.shouldApply(q));
    }

    @Test
    public void shouldHaveZeroProbabilityForFirstSlot() {
        DailySchedule schedule = new DailySchedule(0, h2Min(15), 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(5)), is(0.0));
    }

    @Test
    public void shouldHaveEqualProbabilitiesInFirstPeakEdges() {
        DailySchedule schedule = new DailySchedule(0, h2Min(15), 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(h2Min(2))),
                is(dist.at(schedule.getSlotForMinute(h2Min(4) - 1))));
    }

    @Test
    public void shouldHaveHigherProbabilityInPeakThanSlope() {
        DailySchedule schedule = new DailySchedule(0, h2Min(15), 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(h2Min(3))),
                is(greaterThan(dist.at(schedule.getSlotForMinute(h2Min(4) + 15)))));
    }

    @Test
    public void shouldHaveHigherProbabilityInSlopeThanPlateau() {
        DailySchedule schedule = new DailySchedule(0, h2Min(15), 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(h2Min(4) + 15)),
                is(greaterThan(dist.at(schedule.getSlotForMinute(h2Min(6))))));
    }
}
