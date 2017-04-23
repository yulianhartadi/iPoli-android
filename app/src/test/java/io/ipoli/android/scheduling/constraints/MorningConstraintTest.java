package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.MorningConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class MorningConstraintTest {

    private Constraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new MorningConstraint();
    }

    @Test
    public void shouldHaveEqualNonZeroProbAtStartAndNextSlot() {
        DailySchedule schedule = new DailySchedule(0, 15 * 60, 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(0)),
                is(equalTo(dist.at(schedule.getSlotForMinute(15)))));
        assertThat(dist.at(schedule.getSlotForMinute(0)), is(greaterThan(0.0)));
    }
}
