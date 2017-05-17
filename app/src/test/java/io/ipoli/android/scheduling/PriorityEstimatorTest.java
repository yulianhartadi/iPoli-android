package io.ipoli.android.scheduling;

import org.junit.Before;
import org.junit.Test;

import io.ipoli.android.app.scheduling.PriorityEstimator;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class PriorityEstimatorTest {

    private PriorityEstimator estimator;

    @Before
    public void setUp() throws Exception {
        estimator = new PriorityEstimator();
    }

    @Test
    public void dailyMostImportantShouldHaveMorePriorityThanUrgent() {
        Quest q1 = new Quest("");
        q1.setPriority(Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY);
        Quest q2 = new Quest("");
        q2.setPriority(Quest.PRIORITY_NOT_IMPORTANT_URGENT);

        assertThat(estimator.estimate(q1), is(greaterThan(estimator.estimate(q2))));
    }

    @Test
    public void importantShouldHaveMorePriorityThanUrgent() {
        Quest q1 = new Quest("");
        q1.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        Quest q2 = new Quest("");
        q2.setPriority(Quest.PRIORITY_NOT_IMPORTANT_URGENT);

        assertThat(estimator.estimate(q1), is(greaterThan(estimator.estimate(q2))));
    }

    @Test
    public void preferredStartTimeShouldHaveMorePriorityThanAny() {
        Quest q1 = new Quest("");
        q1.setStartTimePreference(TimePreference.MORNING);
        Quest q2 = new Quest("");
        q2.setStartTimePreference(TimePreference.ANY);

        assertThat(estimator.estimate(q1), is(greaterThan(estimator.estimate(q2))));
    }

    @Test
    public void workCategoryShouldHaveMorePriorityThanFun() {
        Quest q1 = new Quest("", Category.WORK);
        Quest q2 = new Quest("", Category.FUN);

        assertThat(estimator.estimate(q1), is(greaterThan(estimator.estimate(q2))));
    }

    @Test
    public void learningCategoryShouldHaveTheSamePriorityAsWellness() {
        Quest q1 = new Quest("", Category.LEARNING);
        Quest q2 = new Quest("", Category.WELLNESS);

        assertThat(estimator.estimate(q1), is(equalTo(estimator.estimate(q2))));
    }

    @Test
    public void fromChallengeShouldHaveMorePriorityThanFromFunCategory() {
        Quest q1 = new Quest("");
        q1.setChallengeId("1");
        Quest q2 = new Quest("", Category.FUN);

        assertThat(estimator.estimate(q1), is(greaterThan(estimator.estimate(q2))));
    }
}
