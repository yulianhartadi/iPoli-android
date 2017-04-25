package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.DailyScheduleBuilder;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.ProductiveTimeConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.scheduling.distributions.DistributionTestUtil;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/24/17.
 */
public class ProductiveTimeConstraintTest {

    private Constraint productivityConstraint;

    @Before
    public void setUp() {
        productivityConstraint = new ProductiveTimeConstraint(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIME);
    }

    @Test
    public void shouldApplyToWorkTasks() {
        Task t = new Task(30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.WORK);
        assertTrue(productivityConstraint.shouldApply(t));
    }

    @Test
    public void shouldApplyToLearningTasks() {
        Task t = new Task(30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.LEARNING);
        assertTrue(productivityConstraint.shouldApply(t));
    }

    @Test
    public void shouldHaveHighestProbabilityDuringMorning() {
        List<TimeOfDay> productiveTimes = Arrays.asList(TimeOfDay.MORNING);
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(Time.h2Min(9))
                .setEndMinute(Time.MINUTES_IN_A_DAY)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(productiveTimes)
                .createDailySchedule();
        ProductiveTimeConstraint constraint = new ProductiveTimeConstraint(productiveTimes);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist), is(12));
    }

    @Test
    public void shouldHaveHighestProbabilityDuringMorningAndEvening() {
        List<TimeOfDay> productiveTimes = Arrays.asList(TimeOfDay.MORNING, TimeOfDay.EVENING);
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(Time.h2Min(9))
                .setEndMinute(Time.MINUTES_IN_A_DAY)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(productiveTimes)
                .createDailySchedule();
        ProductiveTimeConstraint constraint = new ProductiveTimeConstraint(productiveTimes);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist), is(32));
    }

    @Test
    public void shouldHaveEqualProbabilityAtAnyTime() {
        List<TimeOfDay> productiveTimes = Arrays.asList(TimeOfDay.ANY_TIME);
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(Time.h2Min(9))
                .setEndMinute(Time.MINUTES_IN_A_DAY)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(productiveTimes)
                .createDailySchedule();
        ProductiveTimeConstraint constraint = new ProductiveTimeConstraint(productiveTimes);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist), is(schedule.getSlotCount()));
    }

    @Test
    public void shouldNotHaveHighestProbabilityDuringMorning() {
        List<TimeOfDay> productiveTimes = Arrays.asList(TimeOfDay.MORNING);
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(Time.h2Min(15))
                .setEndMinute(Time.MINUTES_IN_A_DAY)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(productiveTimes)
                .createDailySchedule();
        ProductiveTimeConstraint constraint = new ProductiveTimeConstraint(productiveTimes);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist), is(schedule.getSlotCount()));
    }
}
