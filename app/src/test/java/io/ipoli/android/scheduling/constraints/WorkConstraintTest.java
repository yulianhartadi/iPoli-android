package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import io.ipoli.android.Constants;
import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.DailyScheduleBuilder;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.constraints.WorkConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.utils.TimePreference;
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
    private DailySchedule schedule;

    @Before
    public void setUp() throws Exception {
        constraint = new WorkConstraint(0, 30);
        schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Arrays.asList(TimeOfDay.MORNING))
                .createDailySchedule();
    }

    @Test
    public void shouldNotApplyToNonWorkQuest() {
        Task t = new Task(0, Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT, TimePreference.ANY, Category.WELLNESS);
        assertFalse(constraint.shouldApply(t));
    }

    @Test
    public void shouldApplyToWorkQuest() {
        Task t = new Task(0, Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT, TimePreference.ANY, Category.WORK);
        assertTrue(constraint.shouldApply(t));
    }

    @Test
    public void shouldHaveZeroProbabilityOfPlacingOutsideWorkTime() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(55)), is(0.0));
    }

    @Test
    public void shouldHaveNonZeroProbabilityWithinWorkTime() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(0)), is(greaterThan(0.0)));
    }

    @Test
    public void shouldHaveZeroProbabilityAtEndOfWorkTime() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(30)), is(0.0));
    }
}
