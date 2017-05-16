package io.ipoli.android.scheduling;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.ipoli.android.Constants;
import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.DailyScheduleBuilder;
import io.ipoli.android.app.scheduling.PriorityEstimator;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.TimeBlock;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailyScheduleTest {

    private Random random;
    private Time defaultTime;

    @Before
    public void setUp() throws Exception {
        random = new Random(Constants.RANDOM_SEED);
        defaultTime = Time.of(0);
    }

    @Test
    public void shouldHaveFreeTimeAtStart() {
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setScheduledTasks(Collections.singletonList(new Task(30, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)))
                .createDailySchedule();
        assertTrue(schedule.isFree(0, 30));
        assertFalse(schedule.isFree(30, 60));
    }

    @Test
    public void shouldHaveFreeTimeAtEnd() {
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setScheduledTasks(Collections.singletonList(new Task(0, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)))
                .createDailySchedule();
        assertTrue(schedule.isFree(30, 60));
        assertFalse(schedule.isFree(0, 30));
    }

    @Test
    public void shouldNotHaveFreeTimeAtSlot() {
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setScheduledTasks(Collections.singletonList(new Task(5, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)))
                .createDailySchedule();
        assertTrue(schedule.isFree(30, 60));
        assertFalse(schedule.isFree(0, 30));
    }

    @Test
    public void shouldScheduleTask() {
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE)
                .setEndMinute(Constants.DEFAULT_PLAYER_SLEEP_START_MINUTE)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setSeed(random)
                .createDailySchedule();
        List<Task> tasksToSchedule = new ArrayList<>();
        Quest q = new Quest("q1", Category.WELLNESS);
        q.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        q.setStartTimePreference(TimePreference.MORNING);
        q.setDuration(30);
        tasksToSchedule.add(toTask(q));
        List<Task> scheduledTasks = schedule.scheduleTasks(tasksToSchedule, Time.of(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE));
        assertThat(scheduledTasks.size(), is(1));
        assertThat(scheduledTasks.get(0).getRecommendedSlots().get(0).getStartMinute(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    public void shouldNotOverlapWithScheduledTasks() {
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setScheduledTasks(Collections.singletonList(new Task(0, 20, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)))
                .createDailySchedule();
        List<Task> tasksToSchedule = Collections.singletonList(new Task(20, Quest.PRIORITY_IMPORTANT_URGENT, TimePreference.ANY, Category.CHORES));
        List<Task> scheduledTasks = schedule.scheduleTasks(tasksToSchedule, defaultTime);
        Task scheduledTask = scheduledTasks.get(0);
        List<TimeBlock> recommendedSlots = scheduledTask.getRecommendedSlots();
        assertThat(recommendedSlots.size(), is(1));
    }

    @Test
    public void shouldNotUpdateCurrentSlot() {
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .createDailySchedule();

        List<Task> firstScheduledTasks = schedule.scheduleTasks(
                Collections.singletonList(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                Collections.singletonList(new Task(20, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                defaultTime);


        List<Task> secondScheduledTasks = schedule.scheduleTasks(
                Collections.singletonList(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                Collections.singletonList(new Task(25, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                defaultTime);

        assertTimeBlocksAreEqual(firstScheduledTasks.get(0).getRecommendedSlots().get(0), secondScheduledTasks.get(0).getRecommendedSlots().get(0));

    }

    @Test
    public void shouldUpdateCurrentSlot() {
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .createDailySchedule();

        List<Task> firstScheduledTasks = schedule.scheduleTasks(
                Collections.singletonList(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                Collections.singletonList(new Task(20, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                defaultTime);

        TimeBlock timeBlock = firstScheduledTasks.get(0).getRecommendedSlots().get(0);

        List<Task> secondScheduledTasks = schedule.scheduleTasks(
                Collections.singletonList(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                Collections.singletonList(new Task(timeBlock.getStartMinute(), 10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                defaultTime);

        assertTimeBlocksAreNotEqual(timeBlock, secondScheduledTasks.get(0).getRecommendedSlots().get(0));

    }

    @Test
    public void shouldNotOverlapWithNewTask() {
        DailySchedule schedule = new DailyScheduleBuilder()
                .setStartMinute(0)
                .setEndMinute(120)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .createDailySchedule();

        List<Task> scheduledTasks = Collections.singletonList(new Task(20, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        List<Task> tasksToSchedule = new ArrayList<>();
        tasksToSchedule.add(new Task("1", 10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        schedule.scheduleTasks(tasksToSchedule, scheduledTasks, defaultTime);

        tasksToSchedule.add(new Task("2", 10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        List<Task> secondScheduledTasks = schedule.scheduleTasks(tasksToSchedule, scheduledTasks, defaultTime);

        TimeBlock task1TB = null;
        TimeBlock task2TB = null;
        for (Task t : secondScheduledTasks) {
            if (t.getId().equals("1")) {
                task1TB = t.getRecommendedSlots().get(0);
            } else {
                task2TB = t.getRecommendedSlots().get(0);
            }
        }

        assertTimeBlocksAreNotEqual(task1TB, task2TB);
    }

    private Task toTask(Quest quest) {
        PriorityEstimator priorityEstimator = new PriorityEstimator();
        return new Task(quest.getStartMinute() == null ? -1 : quest.getStartMinute(),
                quest.getDuration(),
                priorityEstimator.estimate(quest),
                quest.getStartTimePreference(),
                quest.getCategoryType());
    }

    private void assertTimeBlocksAreEqual(TimeBlock tb1, TimeBlock tb2) {
        assertThat(tb1.getStartMinute(), is(tb2.getStartMinute()));
        assertThat(tb1.getEndMinute(), is(tb2.getEndMinute()));
    }

    private void assertTimeBlocksAreNotEqual(TimeBlock tb1, TimeBlock tb2) {
        assertThat(tb1.getStartMinute(), is(not(tb2.getStartMinute())));
        assertThat(tb1.getEndMinute(), is(not(tb2.getEndMinute())));
    }
}