package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.MorningConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.utils.Time;

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
        constraint = new MorningConstraint(DailySchedule.DEFAULT_TIME_SLOT_DURATION);
    }

    @Test
    public void shouldHaveLargerProbabilityInMorningThanInEvening() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(MorningConstraint.MORNING_START)),
                is(greaterThan(dist.at(constraint.getSlotForMinute(Time.MINUTES_IN_A_DAY)))));
    }

    @Test
    public void shouldHaveEqualProbabilityAtMorningStart() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(MorningConstraint.MORNING_START)),
                is(equalTo(dist.at(constraint.getSlotForMinute(MorningConstraint.MORNING_START + 15)))));
    }

    @Test
    public void shouldHaveEqualProbabilityAtMorningBounds() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(MorningConstraint.MORNING_START)),
                is(equalTo(dist.at(constraint.getSlotForMinute(MorningConstraint.MORNING_END - 1)))));
    }

    @Test
    public void shouldHaveLargerProbabilityInMorningEndThanAfternoon() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(MorningConstraint.MORNING_END - 1)),
                is(greaterThan(dist.at(constraint.getSlotForMinute(MorningConstraint.MORNING_END)))));
    }
}
