package io.ipoli.android.scheduling.distributions;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;

import static io.ipoli.android.scheduling.distributions.DistributionTestUtil.getIndexCountWithMaxProbability;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class FlatPeakDiscreteDistributionTest {

    private FlatPeakDiscreteDistribution dist;

    @Before
    public void setUp() {
        dist = FlatPeakDiscreteDistribution.create(5, 2, 10, 2, 10);
    }

    @Test
    public void shouldBeSymmetrical() {
        assertThat(dist.at(5), is(equalTo(dist.at(6))));
        assertThat(dist.at(4), is(equalTo(dist.at(7))));
        assertThat(dist.at(3), is(equalTo(dist.at(8))));
        assertThat(dist.at(2), is(equalTo(dist.at(9))));
    }

    @Test
    public void shouldHaveDecliningSlopeAtEnd() {
        assertThat(dist.at(6), is(greaterThan(dist.at(7))));
        assertThat(dist.at(7), is(greaterThan(dist.at(8))));
        assertThat(dist.at(8), is(greaterThan(dist.at(9))));
    }

    @Test
    public void shouldBeCutOffAtStart() {
        FlatPeakDiscreteDistribution dist = FlatPeakDiscreteDistribution.create(0, 2, 10, 2, 10);
        assertThat(dist.at(0), is(equalTo(dist.at(1))));
    }

    @Test
    public void shouldHaveZeroSlope() {
        FlatPeakDiscreteDistribution dist = FlatPeakDiscreteDistribution.create(0, 2, 10, 0, 10);
        assertThat(getIndexCountWithMaxProbability(dist), is(2));
        assertThat(dist.at(2), is(equalTo(dist.at(3))));
        assertThat(dist.at(2), is(equalTo(dist.at(9))));
    }

    @Test
    public void shouldWrapAtTheEnd() {
        FlatPeakDiscreteDistribution dist = FlatPeakDiscreteDistribution.create(9, 2, 10, 0, 10);
        assertThat(getIndexCountWithMaxProbability(dist), is(2));
        assertThat(dist.at(0), is(equalTo(dist.at(9))));
    }
}
