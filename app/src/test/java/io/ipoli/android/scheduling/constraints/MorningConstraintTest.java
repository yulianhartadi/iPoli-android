package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

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
    private DailySchedule schedule;

    @Before
    public void setUp() throws Exception {
        constraint = new MorningConstraint();
        schedule = new DailySchedule(MorningConstraint.MORNING_START, Time.MINUTES_IN_A_DAY, 15);
    }

    @Test
    public void shouldHaveLargerProbabilityInMorningThanInEvening() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(MorningConstraint.MORNING_START)),
                is(greaterThan(dist.at(schedule.getSlotForMinute(Time.MINUTES_IN_A_DAY)))));
    }

    @Test
    public void shouldHaveEqualProbabilityAtMorningStart() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(MorningConstraint.MORNING_START)),
                is(equalTo(dist.at(schedule.getSlotForMinute(MorningConstraint.MORNING_START + 15)))));
    }

    @Test
    public void shouldHaveEqualProbabilityAtMorningBounds() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(MorningConstraint.MORNING_START)),
                is(equalTo(dist.at(schedule.getSlotForMinute(MorningConstraint.MORNING_END - 1)))));
    }

    @Test
    public void shouldHaveLargerProbabilityInMorningEndThanAfternoon() {
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(dist.at(schedule.getSlotForMinute(MorningConstraint.MORNING_END - 1)),
                is(greaterThan(dist.at(schedule.getSlotForMinute(MorningConstraint.MORNING_END)))));
    }

    @Test
    public void shouldStartFromScheduleStartMinute() {
        DailySchedule schedule = new DailySchedule(MorningConstraint.MORNING_START + Time.h2Min(2), Time.MINUTES_IN_A_DAY, 15);
        DiscreteDistribution dist = constraint.apply(schedule);
        assertThat(getIndexCountWithMaxProbability(dist), is(4 * schedule.getSlotCountBetween(0, 60)));

    }

    private int getIndexCountWithMaxProbability(DiscreteDistribution dist) {
        List<Double> freq = dist.toList();
        Double max = Collections.max(freq);
        int idxCount = 0;
        for (int i = 0; i < freq.size(); i++) {
            if (Double.compare(freq.get(i), max) == 0) {
                idxCount++;
            }
        }
        return idxCount;
    }
}
