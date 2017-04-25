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

    private LearningConstraint constraint;
    private DailySchedule schedule;

    @Before
    public void setUp() throws Exception {
        constraint = new LearningConstraint();
        List<TimeOfDay> productiveTimes = Arrays.asList(TimeOfDay.MORNING);
        schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(h2Min(15))
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(productiveTimes)
                .createDailySchedule();
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
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(5)), is(0.0));
    }

    @Test
    public void shouldHaveEqualProbabilitiesInFirstPeakEdges() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(h2Min(2))),
                is(dist.at(schedule.getSlotForMinute(h2Min(4) - 1))));
    }

    @Test
    public void shouldHaveHigherProbabilityInPeakThanSlope() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(h2Min(3))),
                is(greaterThan(dist.at(schedule.getSlotForMinute(h2Min(4) + 15)))));
    }

    @Test
    public void shouldHaveHigherProbabilityInSlopeThanPlateau() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(h2Min(4) + 15)),
                is(greaterThan(dist.at(schedule.getSlotForMinute(h2Min(6))))));
    }
}
