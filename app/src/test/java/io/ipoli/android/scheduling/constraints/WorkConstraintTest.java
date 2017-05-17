package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.WorkConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.utils.Time;
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

    @Before
    public void setUp() throws Exception {
        Set<DayOfWeek> workDays = new HashSet<>(Collections.singleton(LocalDate.now().getDayOfWeek()));
        constraint = new WorkConstraint(0, 30, workDays, 15);
    }

    @Test
    public void shouldNotApplyToNonWorkQuest() {
        Task t = new Task(0, Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT, TimePreference.ANY, Category.WELLNESS);
        assertFalse(constraint.shouldApply(t));
    }

    @Test
    public void shouldApplyToWorkQuest() {
        Task t = new Task(0, Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT, TimePreference.WORK_HOURS, Category.WORK);
        assertTrue(constraint.shouldApply(t));
    }

    @Test
    public void shouldHaveZeroProbabilityOfPlacingOutsideWorkTime() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(55)), is(0.0));
    }

    @Test
    public void shouldHaveNonZeroProbabilityWithinWorkTime() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(0)), is(greaterThan(0.0)));
    }

    @Test
    public void shouldHaveZeroProbabilityAtEndOfWorkTime() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(30)), is(0.0));
    }

    @Test
    public void shouldHaveNonZeroProbabilityWhenOverlapsWithMidnight() {
        Set<DayOfWeek> workDays = new HashSet<>(Collections.singleton(LocalDate.now().getDayOfWeek()));
        Constraint constraint = new WorkConstraint(Time.h2Min(23), Time.h2Min(2), workDays, 15);
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(0), is(greaterThan(0.0)));
        assertThat(dist.at(constraint.getSlotForMinute(Time.h2Min(23))), is(greaterThan(0.0)));
        assertThat(dist.at(constraint.getSlotForMinute(Time.h2Min(22))), is(0.0));
    }
}
