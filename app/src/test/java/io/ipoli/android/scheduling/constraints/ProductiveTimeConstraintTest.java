package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.ProductiveTimeConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Category;
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
        productivityConstraint = new ProductiveTimeConstraint(0, 1);
    }

    @Test
    public void shouldApplyToWorkTasks() {
        Task t = new Task(30, Category.WORK);
        assertTrue(productivityConstraint.shouldApply(t));
    }

    @Test
    public void shouldApplyToLearningTasks() {
        Task t = new Task(30, Category.LEARNING);
        assertTrue(productivityConstraint.shouldApply(t));
    }

    @Test
    public void shouldHaveHighestProbabilityDuringProductiveTime() {
        DailySchedule schedule = new DailySchedule(Time.h2Min(9), Time.h2Min(20), 15);
        ProductiveTimeConstraint constraint = new ProductiveTimeConstraint(Time.h2Min(10), Time.h2Min(14));
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(DistributionTestUtil.getIndexCountWithMaxProbability(dist), is(16));
    }
}
