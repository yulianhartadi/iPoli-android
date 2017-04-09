package io.ipoli.android;

import android.support.annotation.NonNull;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;

import org.junit.BeforeClass;
import org.junit.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.threeten.bp.temporal.TemporalAdjusters.firstDayOfMonth;
import static org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/6/16.
 */
public class RepeatingQuestSchedulerTest {

    private static RepeatingQuestScheduler rqScheduler;
    private static LocalDate today;

    @BeforeClass
    public static void setUp() {
        rqScheduler = new RepeatingQuestScheduler(42);
        today = LocalDate.now();
    }

    @Test
    public void createRepeatingQuestStartingMonday() {
        LocalDate startDate = today.with(DayOfWeek.MONDAY);
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(startDate);
        repeatingQuest.setRecurrence(recurrence);

        LocalDate endDate = today.with(DayOfWeek.SUNDAY);
        List<Quest> result = rqScheduler.schedule(repeatingQuest, startDate, endDate);
        assertThat(result.size(), is(3));
    }

    @Test
    public void createRepeatingQuestStartingTuesday() {
        LocalDate startDate = LocalDate.now().with(DayOfWeek.TUESDAY);
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(startDate);
        repeatingQuest.setRecurrence(recurrence);

        LocalDate endDate = LocalDate.now().with(DayOfWeek.SUNDAY);
        List<Quest> result = rqScheduler.schedule(repeatingQuest, startDate, endDate);
        assertThat(result.size(), is(2));
    }

    @Test
    public void scheduleRepeatingQuestCreatedLastFriday() {
        LocalDate lastFriday = LocalDate.now().with(DayOfWeek.FRIDAY).minusDays(7);
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(lastFriday);
        repeatingQuest.setRecurrence(recurrence);

        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate sunday = LocalDate.now().with(DayOfWeek.SUNDAY);
        List<Quest> result = rqScheduler.schedule(repeatingQuest, monday, sunday);
        assertThat(result.size(), is(3));
    }

    @Test
    public void scheduleDailyRepeatingQuest() {
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(today);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(repeatingQuest, today, today.with(DayOfWeek.SUNDAY));
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleMonthlyRepeatingQuestAtStartOfMonth() {
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        recurrence.setDtstartDate(startOfMonth);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(13);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(repeatingQuest, startOfMonth, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleMonthlyRepeatingQuestAfterMiddleOfMonth() {
        LocalDate midOfMonth = today.withDayOfMonth(15);
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(midOfMonth);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(13);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(repeatingQuest, midOfMonth, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(0));
    }

    @Test
    public void scheduleQuestForSingleDay() {
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(today);
        Recur recur = createEveryDayRecur();
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(repeatingQuest, today, today);
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleQuestForFullWeek() {
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(startOfWeek);
        Recur recur = createEveryDayRecur();
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(repeatingQuest, startOfWeek, endOfWeek);
        assertThat(result.size(), is(7));
    }

    @Test
    public void scheduleRepeatingQuestWithEndDate() {
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        recurrence.setDtstartDate(startOfWeek);
        LocalDate tomorrow = startOfWeek.plusDays(1);
        recurrence.setDtendDate(tomorrow);
        Recur recur = createEveryDayRecur();
        recurrence.setRrule(recur.toString());
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfWeek, today.with(DayOfWeek.SUNDAY));
        assertThat(result.size(), is(2));
    }

    @Test
    public void scheduleFlexibleWeeklyQuest() {
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setRrule(createEveryDayRecur().toString());
        recurrence.setDtstartDate(startOfWeek);
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfWeek, startOfWeek.with(DayOfWeek.SUNDAY));
        assertThat(result.size(), is(3));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithNotEnoughDaysToSchedule() {
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setRrule(createEveryDayRecur().toString());
        recurrence.setDtstartDate(endOfWeek);
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, endOfWeek, endOfWeek);
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithNotEnoughPreferredDays() {
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.WE);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(startOfWeek);
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfWeek, today.with(DayOfWeek.SUNDAY));
        assertThat(result.size(), is(3));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithNoPreferredDays() {
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(startOfWeek);
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfWeek, today.with(DayOfWeek.SUNDAY));
        assertThat(result.size(), is(3));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithPassedPreferredDays() {
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(endOfWeek);
        recurrence.setFlexibleCount(2);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, endOfWeek, endOfWeek);
        assertThat(result.size(), is(0));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithNotEnoughPreferredDaysAtStartOfWeekend() {
        LocalDate saturday = today.with(DayOfWeek.SATURDAY);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(saturday);
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, saturday, today.with(DayOfWeek.SUNDAY));
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithTimesADay() {
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        RepeatingQuest rq = createRepeatingQuest();
        rq.setTimesADay(3);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setRrule(createEveryDayRecur().toString());
        recurrence.setDtstartDate(startOfWeek);
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfWeek, today.with(DayOfWeek.SUNDAY));
        assertThat(result.size(), is(3));
        assertThat(result.get(0).getTimesADay(), is(3));
    }

    @Test
    public void scheduleFlexibleMonthlyQuest() {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(startOfMonth);
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfMonth, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(15));
    }

    @Test
    public void scheduleFlexibleMonthlyQuestWithPreferredDays() {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(startOfMonth);
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfMonth, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(15));
        for (Quest q : result) {
            DayOfWeek dayOfWeek = q.getEndDate().getDayOfWeek();
            assertThat(dayOfWeek, is(not(DayOfWeek.SATURDAY)));
            assertThat(dayOfWeek, is(not(DayOfWeek.SUNDAY)));
        }
    }

    @Test
    public void scheduleFlexibleMonthlyQuestWithNotEnoughPreferredDays() {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(startOfMonth);
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfMonth, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(15));
        List<Quest> mon = new ArrayList<>();
        List<Quest> tue = new ArrayList<>();
        for (Quest q : result) {
            DayOfWeek dayOfWeek = q.getEndDate().getDayOfWeek();
            if (dayOfWeek == DayOfWeek.MONDAY) {
                mon.add(q);
            }
            if (dayOfWeek == DayOfWeek.TUESDAY) {
                tue.add(q);
            }
        }
        assertThat(mon.size(), is(greaterThanOrEqualTo(4)));
        assertThat(tue.size(), is(greaterThanOrEqualTo(4)));
    }

    @Test
    public void scheduleFlexibleMonthlyQuestNotAllAtStartOfMonth() {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(startOfMonth);
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfMonth, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(15));
        Collections.sort(result, (q1, q2) -> {
            if (q1.getEndDate().isBefore(q2.getEndDate())) {
                return -1;
            }
            return 1;
        });
        int maxDayOfMonth = result.get(result.size() - 1).getEndDate().getDayOfMonth();
        assertThat(maxDayOfMonth, is(not(15)));
    }

    @Test
    public void scheduleFlexibleMonthlyQuestWithNotAllFirstPreferredDays() {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(startOfMonth);
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, startOfMonth, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(15));
        Set<Integer> weekNumbers = new HashSet<>();
        for (Quest q : result) {
            weekNumbers.add(q.getEndDate().getDayOfWeek().getValue() % 7);
        }
        assertThat(weekNumbers.size(), is(greaterThanOrEqualTo(4)));
    }

    @Test
    public void scheduleFlexibleMonthlyQuestAtTheMiddleOfMonth() {
        LocalDate middleOfMonth = today.withDayOfMonth(1).plusDays(15);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(middleOfMonth);
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, middleOfMonth, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(lessThanOrEqualTo(11)));
        for (Quest q : result) {
           assertThat(q.getEndDate(), is(greaterThanOrEqualTo(middleOfMonth)));
        }
    }

    @Test
    public void scheduleFlexibleMonthlyQuestAtTheMiddleOfMonthWithPreferredDays() {
        LocalDate middleMonday = today.withDayOfMonth(1).plusDays(15).with(DayOfWeek.MONDAY);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(middleMonday);
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.schedule(rq, middleMonday, today.with(lastDayOfMonth()));
        assertThat(result.size(), is(lessThanOrEqualTo(11)));

        for (Quest q : result) {
            assertThat(q.getEndDate(), is(greaterThanOrEqualTo(middleMonday)));
        }
    }

    @Test
    public void scheduleFlexibleMonthlyQuestWithTimesADay() {
        LocalDate startOfMonth = LocalDate.now().with(firstDayOfMonth());
        RepeatingQuest rq = createRepeatingQuest();
        rq.setTimesADay(3);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(startOfMonth);
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = rqScheduler.scheduleAhead(rq, startOfMonth);
        assertThat(result.size(), is(15));
        assertThat(result.get(0).getTimesADay(), is(3));
    }

    @NonNull
    private Recur createEveryDayRecur() {
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        recur.getDayList().add(WeekDay.SA);
        recur.getDayList().add(WeekDay.SU);
        return recur;
    }

    @NonNull
    private RepeatingQuest createRepeatingQuest() {
        RepeatingQuest repeatingQuest = new RepeatingQuest("Test");
        repeatingQuest.setCategory(Category.CHORES.name());
        return repeatingQuest;
    }

    @NonNull
    private Recurrence createWeeklyRecurrence(LocalDate startDate) {
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(startDate);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        return recurrence;
    }
}