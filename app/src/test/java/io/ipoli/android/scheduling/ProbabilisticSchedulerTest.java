package io.ipoli.android.scheduling;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.ipoli.android.app.scheduling.ProbabilisticTaskScheduler;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.TimeBlock;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class ProbabilisticSchedulerTest {

    private static Time startTime;
    private static Random random;

    @BeforeClass
    public static void setUp() {
        startTime = Time.at(0, 0);
        random = new Random(42);
    }

    @Test
    public void shouldGiveAllAvailableSlots() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(10, 20, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));

        ProbabilisticTaskScheduler scheduler = new ProbabilisticTaskScheduler(0, 1, tasks, random);

        double[] values = new double[61];
        for (int i = 0; i < values.length; i++) {
            values[i] = random.nextInt(100);
        }
        DiscreteDistribution dist = new DiscreteDistribution(values);

        List<TimeBlock> slots = scheduler.chooseSlotsFor(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL), 15, startTime, dist);
        assertThat(slots.size(), is(3));
    }
}
