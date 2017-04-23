package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.NotWorkConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.quest.data.Category;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */
public class NotWorkConstraintTest {

    private Constraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new NotWorkConstraint(0, 30);
    }

    @Test
    public void shouldApplyToNonWorkTask() {
        Task t = new Task(30, Category.WELLNESS);
        assertTrue(constraint.shouldApply(t));
    }

    @Test
    public void shouldNotApplyToWorkTask() {
        Task t = new Task(30, Category.WORK);
        assertFalse(constraint.shouldApply(t));
    }

    @Test
    public void shouldHaveNonZeroProbabilityOutsideWorkTime() {
        DailySchedule schedule = new DailySchedule(0, 60, 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(55)), is(greaterThan(0.0)));
    }

    @Test
    public void shouldHaveZeroProbabilityWithinWorkTime() {
        DailySchedule schedule = new DailySchedule(0, 60, 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(0)), is(0.0));
    }

    @Test
    public void shouldHaveNonZeroProbabilityAtStartOfNonWorkTime() {
        DailySchedule schedule = new DailySchedule(0, 60, 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(30)), is(greaterThan(0.0)));
    }
}
