package io.ipoli.android.scheduling;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import io.ipoli.android.Constants;
import io.ipoli.android.app.scheduling.DailyScheduler;
import io.ipoli.android.app.scheduling.DailySchedulerBuilder;
import io.ipoli.android.app.scheduling.PriorityEstimator;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.TimeSlot;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.QuestTask;

import static io.ipoli.android.app.utils.Time.h2Min;
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
public class DailySchedulerTest {

    private Random random;
    private Time defaultTime;

    @Before
    public void setUp() throws Exception {
        random = new Random(Constants.RANDOM_SEED);
        defaultTime = Time.of(0);
    }

    @Test
    public void shouldNotScheduleInSleepMinutes() {
        int sleepStart = h2Min(2);
        int sleepEnd = h2Min(6);
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        schedule.scheduleTasks(new ArrayList<>());
        assertFalse(schedule.isFree(sleepStart, sleepEnd));
    }

    @Test
    public void shouldNotScheduleInDefaultSleepMinutes() {
        int sleepStart = Constants.DEFAULT_PLAYER_SLEEP_START_MINUTE;
        int sleepEnd = Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE;
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        schedule.scheduleTasks(new ArrayList<>());
        assertFalse(schedule.isFree(sleepStart, sleepEnd));
    }

    @Test
    public void shouldNotScheduleWithinSleepMinutes() {
        int sleepStart = h2Min(23);
        int sleepEnd = h2Min(8);
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        schedule.scheduleTasks(new ArrayList<>());
        assertFalse(schedule.isFree(sleepStart + 60, sleepEnd - 60));
    }

    @Test
    public void shouldNotScheduleOnStartBound() {
        int sleepStart = h2Min(23);
        int sleepEnd = h2Min(8);
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        schedule.scheduleTasks(new ArrayList<>());
        assertFalse(schedule.isFree(sleepStart, sleepEnd + 60));
    }

    @Test
    public void shouldNotScheduleOnEndBound() {
        int sleepStart = h2Min(23);
        int sleepEnd = h2Min(8);
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        schedule.scheduleTasks(new ArrayList<>());
        assertFalse(schedule.isFree(sleepStart - 60, sleepEnd));
    }

    @Test
    public void shouldScheduleOnStartBound() {
        int sleepStart = h2Min(23);
        int sleepEnd = h2Min(8);
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        schedule.scheduleTasks(new ArrayList<>());
        assertTrue(schedule.isFree(sleepStart - 60, sleepStart));
    }

    @Test
    public void shouldHaveTaskAtSleepingTime() {
        int sleepStart = h2Min(23);
        int sleepEnd = h2Min(8);
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        List<Task> scheduledTasks = Collections.singletonList(new Task(0, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        schedule.scheduleTasks(new ArrayList<>(), scheduledTasks);
        assertTrue(schedule.isFree(sleepEnd, sleepStart));
    }

    @Test
    public void shouldOccupySlotsForTaskOverlappingWithSleepStart() {
        int sleepStart = h2Min(1);
        int sleepEnd = h2Min(8);
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        List<Task> scheduledTasks = Collections.singletonList(new Task(0, h2Min(2), Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        schedule.scheduleTasks(new ArrayList<>(), scheduledTasks);
        assertFalse(schedule.isFree(0, sleepStart));
        assertTrue(schedule.isFree(sleepEnd, 0));
        assertFalse(schedule.isFree(h2Min(23), sleepStart));
    }

    @Test
    public void shouldOccupySlotsForTaskOverlappingWithSleepEnd() {
        int sleepStart = h2Min(1);
        int sleepEnd = h2Min(8);
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        List<Task> scheduledTasks = Collections.singletonList(new Task(h2Min(7), h2Min(2), Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        schedule.scheduleTasks(new ArrayList<>(), scheduledTasks);
        assertFalse(schedule.isFree(sleepEnd, sleepEnd + h2Min(1)));
        assertTrue(schedule.isFree(sleepEnd + h2Min(1), sleepStart));
        assertFalse(schedule.isFree(sleepStart, sleepStart + h2Min(2)));
    }

    @Test
    public void shouldHaveFreeTimeAtStart() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        List<Task> scheduledTasks = Collections.singletonList(new Task(30, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        schedule.scheduleTasks(new ArrayList<>(), scheduledTasks);
        assertTrue(schedule.isFree(0, 30));
        assertFalse(schedule.isFree(30, 60));
    }

    @Test
    public void shouldHaveFreeTimeAtEnd() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        List<Task> scheduledTasks = Collections.singletonList(new Task(0, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        schedule.scheduleTasks(new ArrayList<>(), scheduledTasks);
        assertTrue(schedule.isFree(30, 60));
        assertFalse(schedule.isFree(0, 30));
    }

    @Test
    public void shouldNotHaveFreeTimeAtSlot() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        List<Task> scheduledTasks = Collections.singletonList(new Task(5, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        schedule.scheduleTasks(new ArrayList<>(), scheduledTasks);
        assertTrue(schedule.isFree(30, 60));
        assertFalse(schedule.isFree(0, 30));
    }

    @Test
    public void shouldScheduleWorkTaskInWorkRange() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(h2Min(22))
                .setEndMinute(h2Min(6))
                .setWorkStartMinute(h2Min(23))
                .setWorkEndMinute(h2Min(2))
                .setWorkDays(new HashSet<>(Collections.singleton(LocalDate.now().getDayOfWeek())))
                .setSeed(Constants.RANDOM_SEED)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        List<Task> tasksToSchedule = Collections.singletonList(new Task(90, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.WORK));
        List<Task> tasks = schedule.scheduleTasks(tasksToSchedule, Time.of(h2Min(22)));
        assertThat(tasks.get(0).getCurrentTimeSlot().getStartMinute(), is(greaterThanOrEqualTo(h2Min(23))));
    }

    @Test
    public void shouldScheduleTask() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE)
                .setEndMinute(Constants.DEFAULT_PLAYER_SLEEP_START_MINUTE)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setSeed(random)
                .create();
        List<Task> tasksToSchedule = new ArrayList<>();
        Quest q = new Quest("q1", Category.WELLNESS);
        q.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        q.setStartTimePreference(TimePreference.MORNING);
        q.setDuration(30);
        tasksToSchedule.add(toTask(q));
        List<Task> scheduledTasks = schedule.scheduleTasks(tasksToSchedule, Time.of(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE));
        assertThat(scheduledTasks.size(), is(1));
        assertThat(scheduledTasks.get(0).getCurrentTimeSlot().getStartMinute(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    public void shouldNotOverlapWithScheduledTasks() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        List<Task> scheduledTasks = Collections.singletonList(new Task(0, 20, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        List<Task> tasksToSchedule = Collections.singletonList(new Task(20, Quest.PRIORITY_IMPORTANT_URGENT, TimePreference.ANY, Category.CHORES));
        List<Task> tasks = schedule.scheduleTasks(tasksToSchedule, scheduledTasks, defaultTime);
        Task scheduledTask = tasks.get(0);
        TimeSlot timeSlot = scheduledTask.getCurrentTimeSlot();
        TimeSlot newTimeSlot = schedule.chooseNewTimeSlot(scheduledTask.getId(), Time.of(0)).getCurrentTimeSlot();
        assertTimeSlotsAreEqual(timeSlot, newTimeSlot);
    }

    @Test
    public void shouldNotUpdateCurrentSlot() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();

        List<Task> firstScheduledTasks = schedule.scheduleTasks(
                Collections.singletonList(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                Collections.singletonList(new Task(20, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                defaultTime);


        List<Task> secondScheduledTasks = schedule.scheduleTasks(
                Collections.singletonList(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                Collections.singletonList(new Task(25, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                defaultTime);

        assertTimeSlotsAreEqual(firstScheduledTasks.get(0).getCurrentTimeSlot(), secondScheduledTasks.get(0).getCurrentTimeSlot());

    }

    @Test
    public void shouldUpdateCurrentSlot() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();

        List<Task> firstScheduledTasks = schedule.scheduleTasks(
                Collections.singletonList(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                Collections.singletonList(new Task(20, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                defaultTime);

        TimeSlot timeSlot = firstScheduledTasks.get(0).getCurrentTimeSlot();

        List<Task> secondScheduledTasks = schedule.scheduleTasks(
                Collections.singletonList(new Task(10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                Collections.singletonList(new Task(timeSlot.getStartMinute(), 10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL)),
                defaultTime);

        assertTimeSlotsAreNotEqual(timeSlot, secondScheduledTasks.get(0).getCurrentTimeSlot());

    }

    @Test
    public void shouldNotOverlapWithNewTask() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(0)
                .setEndMinute(120)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();

        List<Task> scheduledTasks = Collections.singletonList(new Task(20, 30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        List<Task> tasksToSchedule = new ArrayList<>();
        tasksToSchedule.add(new Task("1", 10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        schedule.scheduleTasks(tasksToSchedule, scheduledTasks, defaultTime);

        tasksToSchedule.add(new Task("2", 10, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        List<Task> secondScheduledTasks = schedule.scheduleTasks(tasksToSchedule, scheduledTasks, defaultTime);

        TimeSlot task1TB = null;
        TimeSlot task2TB = null;
        for (Task t : secondScheduledTasks) {
            if (t.getId().equals("1")) {
                task1TB = t.getCurrentTimeSlot();
            } else {
                task2TB = t.getCurrentTimeSlot();
            }
        }

        assertTimeSlotsAreNotEqual(task1TB, task2TB);
    }

    @Test
    public void shouldScheduleTaskForNextSlot() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(0)
                .setEndMinute(60)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setSeed(Constants.RANDOM_SEED)
                .create();
        List<Task> tasksToSchedule = Collections.singletonList(new Task(15, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.ANY, Category.PERSONAL));
        List<Task> scheduledTasks = schedule.scheduleTasks(tasksToSchedule, Time.of(0));
        Task t = scheduledTasks.get(0);
        TimeSlot oldTimeSlot = t.getCurrentTimeSlot();
        Task updatedTask = schedule.chooseNewTimeSlot(t.getId(), Time.of(0));
        assertTimeSlotsAreNotEqual(oldTimeSlot, updatedTask.getCurrentTimeSlot());
    }

    @Test
    public void shouldHaveFreeSlotsWhenSleepEndIsNotExactSlot() {
        int sleepStart = h2Min(0);
        int sleepEnd = h2Min(1) + 20;
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(sleepEnd)
                .setEndMinute(sleepStart)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .create();
        schedule.scheduleTasks(new ArrayList<>());
        assertFalse(schedule.isFree(0, sleepEnd - 5));
        assertTrue(schedule.isFree(sleepEnd + 10, h2Min(2)));
    }

    @Test
    public void shouldNotMoveTask() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE)
                .setEndMinute(Constants.DEFAULT_PLAYER_SLEEP_START_MINUTE)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setSeed(random)
                .create();

        Quest q = new Quest("q1", Category.WELLNESS);
        q.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        q.setStartTimePreference(TimePreference.MORNING);
        q.setDuration(30);

        List<Task> tasks = schedule.scheduleTasks(Collections.singletonList(new QuestTask("id", q.getDuration(), q.getPriority(), q.getStartTimePreference(), q.getCategoryType(), q)), Time.of(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE));

        Quest q2 = new Quest("q1", Category.WELLNESS);
        q2.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        q2.setStartTimePreference(TimePreference.MORNING);
        q2.setDuration(30);

        List<Task> updatedTasks = schedule.scheduleTasks(Collections.singletonList(new QuestTask("id", q2.getDuration(), q2.getPriority(), q2.getStartTimePreference(), q2.getCategoryType(), q2)));

        assertThat(updatedTasks.get(0).getCurrentTimeSlot().getStartMinute(), is(tasks.get(0).getCurrentTimeSlot().getStartMinute()));
    }

    @Test
    @Ignore
    public void shouldMoveTask() {
        DailyScheduler schedule = new DailySchedulerBuilder()
                .setStartMinute(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE)
                .setEndMinute(Constants.DEFAULT_PLAYER_SLEEP_START_MINUTE)
                .setWorkStartMinute(Constants.DEFAULT_PLAYER_WORK_START_MINUTE)
                .setWorkEndMinute(Constants.DEFAULT_PLAYER_WORK_END_MINUTE)
                .setProductiveTimes(Constants.DEFAULT_PLAYER_PRODUCTIVE_TIMES)
                .setSeed(random)
                .create();

        Quest q = new Quest("q1", Category.WELLNESS);
        q.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        q.setStartTimePreference(TimePreference.MORNING);
        q.setDuration(30);

        List<Task> tasks = schedule.scheduleTasks(Collections.singletonList(new QuestTask("id", q.getDuration(), q.getPriority(), q.getStartTimePreference(), q.getCategoryType(), q)), Time.of(Constants.DEFAULT_PLAYER_SLEEP_END_MINUTE));

        Quest q2 = new Quest("q1", Category.WELLNESS);
        q2.setPriority(Quest.PRIORITY_IMPORTANT_NOT_URGENT);
        q2.setStartTimePreference(TimePreference.MORNING);
        q2.setDuration(20);

        List<Task> updatedTasks = schedule.scheduleTasks(Collections.singletonList(new QuestTask("id", q2.getDuration(), q2.getPriority(), q2.getStartTimePreference(), q2.getCategoryType(), q2)));

        assertThat(updatedTasks.get(0).getCurrentTimeSlot().getStartMinute(), is(not(tasks.get(0).getCurrentTimeSlot().getStartMinute())));
    }

    private Task toTask(Quest quest) {
        PriorityEstimator priorityEstimator = new PriorityEstimator();
        return new Task(quest.getStartMinute() == null ? -1 : quest.getStartMinute(),
                quest.getDuration(),
                priorityEstimator.estimate(quest),
                quest.getStartTimePreference(),
                quest.getCategoryType());
    }

    private void assertTimeSlotsAreEqual(TimeSlot ts1, TimeSlot ts2) {
        assertThat(ts1.getStartMinute(), is(ts2.getStartMinute()));
        assertThat(ts1.getEndMinute(), is(ts2.getEndMinute()));
    }

    private void assertTimeSlotsAreNotEqual(TimeSlot ts1, TimeSlot ts2) {
        assertThat(ts1.getStartMinute(), is(not(ts2.getStartMinute())));
        assertThat(ts1.getEndMinute(), is(not(ts2.getEndMinute())));
    }
}