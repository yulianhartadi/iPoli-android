package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.constraints.WorkConstraint;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */

public class WorkConstraintTest {

    private WorkConstraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new WorkConstraint(0, 30);
    }

    @Test
    public void shouldNotApplyToNonWorkQuest() {
        Quest q = new Quest("q1", Category.WELLNESS);
        assertFalse(constraint.shouldApply(q));
    }

    @Test
    public void shouldApplyToWorkQuest() {
        Quest q = new Quest("q1", Category.WORK);
        assertTrue(constraint.shouldApply(q));
    }

    @Test
    public void shouldHaveZeroProbabilityOfPlacingOutsideWorkTime() {
        DailySchedule schedule = new DailySchedule(0, 60, 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(55)), is(0.0));
    }

    @Test
    public void shouldHaveNonZeroProbabilityWithinWorkTime() {
        DailySchedule schedule = new DailySchedule(0, 60, 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(0)), is(greaterThan(0.0)));
    }

    @Test
    public void shouldHaveZeroProbabilityAtEndOfWorkTime() {
        DailySchedule schedule = new DailySchedule(0, 60, 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(30)), is(0.0));
    }
}
