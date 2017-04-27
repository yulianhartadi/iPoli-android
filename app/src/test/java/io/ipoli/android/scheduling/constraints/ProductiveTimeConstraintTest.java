package io.ipoli.android.scheduling.constraints;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.EveningConstraint;
import io.ipoli.android.app.scheduling.constraints.MorningConstraint;
import io.ipoli.android.app.scheduling.constraints.ProductiveTimeConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
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
        productivityConstraint = new ProductiveTimeConstraint(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES, DailySchedule.DEFAULT_TIME_SLOT_DURATION);
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
        ProductiveTimeConstraint constraint = createConstraint(Arrays.asList(TimeOfDay.MORNING));
        DiscreteDistribution dist = constraint.apply();
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist), is(getMorningSlotCount(constraint)));
    }

    @Test
    public void shouldHaveHighestProbabilityDuringMorningAndEvening() {
        ProductiveTimeConstraint constraint = createConstraint(Arrays.asList(TimeOfDay.MORNING, TimeOfDay.EVENING));
        DiscreteDistribution dist = constraint.apply();
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist), is(getMorningSlotCount(constraint) + getEveningSlotCount(constraint)));
    }


    @Test
    public void shouldHaveEqualProbabilityAtAnyTime() {
        ProductiveTimeConstraint constraint = createConstraint(Arrays.asList(TimeOfDay.ANY_TIME));
        DiscreteDistribution dist = constraint.apply();
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist), is(constraint.getTotalSlotCount()));
    }

    @Test
    public void shouldNotHaveHighestProbabilityDuringMorning() {
        ProductiveTimeConstraint constraint = createConstraint(Arrays.asList(TimeOfDay.MORNING));
        DiscreteDistribution dist = constraint.apply();
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist),
                is(getMorningSlotCount(constraint)));
    }

    private int getMorningSlotCount(ProductiveTimeConstraint constraint) {
        return constraint.getSlotCountBetween(MorningConstraint.MORNING_START, MorningConstraint.MORNING_END);
    }

    private int getEveningSlotCount(ProductiveTimeConstraint constraint) {
        return constraint.getSlotCountBetween(EveningConstraint.EVENING_START, EveningConstraint.EVENING_END);
    }

    @NonNull
    private ProductiveTimeConstraint createConstraint(List<TimeOfDay> productiveTimes) {
        return new ProductiveTimeConstraint(productiveTimes, DailySchedule.DEFAULT_TIME_SLOT_DURATION);
    }
}
