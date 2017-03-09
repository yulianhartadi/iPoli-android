package io.ipoli.android;

import android.support.annotation.NonNull;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/6/16.
 */
public class RepeatingQuestSchedulerTest {

    private static RepeatingQuestScheduler repeatingQuestScheduler;

    @BeforeClass
    public static void setUp() {
        repeatingQuestScheduler = new RepeatingQuestScheduler(42);
    }

    @Test
    public void createRepeatingQuestStartingMonday() {
        Date monday = toStartOfDayUTC(LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY));
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(monday);
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, monday);
        assertThat(result.size(), is(3));
    }

    @Test
    public void createRepeatingQuestStartingTuesday() {
        Date tuesday = toStartOfDayUTC(LocalDate.now().withDayOfWeek(DateTimeConstants.TUESDAY));
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(tuesday);
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, tuesday);
        assertThat(result.size(), is(2));
    }

    @Test
    public void scheduleRepeatingQuestCreatedLastFriday() {
        Date lastFriday = toStartOfDayUTC(LocalDate.now().withDayOfWeek(DateTimeConstants.FRIDAY).minusDays(7));
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(lastFriday);
        repeatingQuest.setRecurrence(recurrence);

        Date monday = toStartOfDayUTC(LocalDate.now().dayOfWeek().withMinimumValue());
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, monday);
        assertThat(result.size(), is(3));
    }

    @NonNull
    private Recurrence createWeeklyRecurrence(Date start) {
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(start);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        return recurrence;
    }

    @Test
    public void scheduleDailyRepeatingQuest() {
        Date today = toStartOfDayUTC(LocalDate.now());
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(today);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, today);
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleMonthlyRepeatingQuestAtStartOfMonth() {
        Date startOfMonth = toStartOfDayUTC(LocalDate.now().withDayOfMonth(1));
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(startOfMonth);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(13);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, startOfMonth);
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleMonthlyRepeatingQuestAfterMiddleOfMonth() {
        Date midOfMonth = toStartOfDayUTC(LocalDate.now().withDayOfMonth(15));
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(midOfMonth);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(13);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, midOfMonth);
        assertThat(result.size(), is(0));
    }

    @Test
    public void scheduleQuestForSingleDay() {
        Date today = toStartOfDayUTC(LocalDate.now());
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(today);
        Recur recur = createEveryDayRecur();
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.scheduleForDateRange(repeatingQuest, today, today);
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleQuestForFullWeek() {
        Date startOfWeek = toStartOfDayUTC(LocalDate.now().dayOfWeek().withMinimumValue());
        Date endOfWeek = toStartOfDayUTC(LocalDate.now().dayOfWeek().withMaximumValue());
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstartDate(startOfWeek);
        Recur recur = createEveryDayRecur();
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.scheduleForDateRange(repeatingQuest, startOfWeek, endOfWeek);
        assertThat(result.size(), is(7));
    }

    @Test
    public void scheduleRepeatingQuestWithTimesADay() {
        Date today = toStartOfDayUTC(LocalDate.now());
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstartDate(today);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, today);
        assertThat(result.size(), is(4));
    }

    @Test
    public void scheduleRepeatingQuestWithEndDate() {
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        LocalDate startOfWeek = LocalDate.now().dayOfWeek().withMinimumValue();
        recurrence.setDtstartDate(toStartOfDayUTC(startOfWeek));
        LocalDate tomorrow = startOfWeek.plusDays(1);
        recurrence.setDtendDate(toStartOfDayUTC(tomorrow));
        Recur recur = createEveryDayRecur();
        recurrence.setRrule(recur.toString());
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfWeek));
        assertThat(result.size(), is(2));
    }

    @Test
    public void scheduleFlexibleWeeklyQuest() {
        LocalDate startOfWeek = LocalDate.now().dayOfWeek().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setRrule(createEveryDayRecur().toString());
        recurrence.setDtstartDate(toStartOfDayUTC(startOfWeek));
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfWeek));
        assertThat(result.size(), is(3));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithNotEnoughDaysToSchedule() {
        LocalDate endOfWeek = LocalDate.now().dayOfWeek().withMaximumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setRrule(createEveryDayRecur().toString());
        recurrence.setDtstartDate(toStartOfDayUTC(endOfWeek));
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(endOfWeek));
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithNotEnoughPreferredDays() {
        LocalDate startOfWeek = LocalDate.now().dayOfWeek().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.WE);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(startOfWeek));
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfWeek));
        assertThat(result.size(), is(3));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithNoPreferredDays() {
        LocalDate startOfWeek = LocalDate.now().dayOfWeek().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(startOfWeek));
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfWeek));
        assertThat(result.size(), is(3));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithPassedPreferredDays() {
        LocalDate endOfWeek = LocalDate.now().dayOfWeek().withMaximumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(endOfWeek));
        recurrence.setFlexibleCount(2);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(endOfWeek));
        assertThat(result.size(), is(0));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithNotEnoughPreferredDaysAtStartOfWeekend() {
        LocalDate saturday = LocalDate.now().dayOfWeek().withMaximumValue().minusDays(1);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(saturday));
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(saturday));
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleFlexibleWeeklyQuestWithTimesADay() {
        LocalDate startOfWeek = LocalDate.now().dayOfWeek().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        rq.setTimesADay(3);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setRrule(createEveryDayRecur().toString());
        recurrence.setDtstartDate(toStartOfDayUTC(startOfWeek));
        recurrence.setFlexibleCount(3);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfWeek));
        assertThat(result.size(), is(3 * 3));
    }

    @Test
    public void scheduleFlexibleMonthlyQuest() {
        LocalDate startOfMonth = LocalDate.now().dayOfMonth().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(startOfMonth));
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfMonth));
        assertThat(result.size(), is(15));
    }

    @Test
    public void scheduleFlexibleMonthlyQuestWithPreferredDays() {
        LocalDate startOfMonth = LocalDate.now().dayOfMonth().withMinimumValue();
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
        recurrence.setDtstartDate(toStartOfDayUTC(startOfMonth));
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfMonth));
        assertThat(result.size(), is(15));
        for (Quest q : result) {
            int dayOfWeek = new LocalDate(q.getEndDate(), DateTimeZone.UTC).getDayOfWeek();
            assertThat(dayOfWeek, is(not(DateTimeConstants.SATURDAY)));
            assertThat(dayOfWeek, is(not(DateTimeConstants.SUNDAY)));
        }
    }

    @Test
    public void scheduleFlexibleMonthlyQuestWithNotEnoughPreferredDays() {
        LocalDate startOfMonth = LocalDate.now().dayOfMonth().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(startOfMonth));
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfMonth));
        assertThat(result.size(), is(15));
        List<Quest> mon = new ArrayList<>();
        List<Quest> tue = new ArrayList<>();
        for (Quest q : result) {
            int dayOfWeek = new LocalDate(q.getEndDate(), DateTimeZone.UTC).getDayOfWeek();
            if (dayOfWeek == DateTimeConstants.MONDAY) {
                mon.add(q);
            }
            if (dayOfWeek == DateTimeConstants.TUESDAY) {
                tue.add(q);
            }
        }
        assertThat(mon.size(), is(greaterThanOrEqualTo(4)));
        assertThat(tue.size(), is(greaterThanOrEqualTo(4)));
    }

    @Test
    public void scheduleFlexibleMonthlyQuestNotAllAtStartOfMonth() {
        LocalDate startOfMonth = LocalDate.now().dayOfMonth().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(startOfMonth));
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfMonth));
        assertThat(result.size(), is(15));
        Collections.sort(result, (q1, q2) -> {
            if (q1.getEndDate().before(q2.getEndDate())) {
                return -1;
            }
            return 1;
        });
        int maxDayOfMonth = new LocalDate(result.get(result.size() - 1).getEndDate(), DateTimeZone.UTC).getDayOfMonth();
        assertThat(maxDayOfMonth, is(not(15)));
    }

    @Test
    public void scheduleFlexibleMonthlyQuestWithNotAllFirstPreferredDays() {
        LocalDate startOfMonth = LocalDate.now().dayOfMonth().withMinimumValue();
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
        recurrence.setDtstartDate(toStartOfDayUTC(startOfMonth));
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfMonth));
        assertThat(result.size(), is(15));
        Set<Integer> weekNumbers = new HashSet<>();
        for (Quest q : result) {
            weekNumbers.add(new LocalDate(q.getEndDate(), DateTimeZone.UTC).getDayOfWeek() % 7);
        }
        assertThat(weekNumbers.size(), is(greaterThanOrEqualTo(4)));
    }

    @Test
    public void scheduleFlexibleMonthlyQuestAtTheMiddleOfMonth() {
        LocalDate middleOfMonth = LocalDate.now().dayOfMonth().withMinimumValue().plusDays(15);
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(middleOfMonth));
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(middleOfMonth));
        assertThat(result.size(), is(lessThanOrEqualTo(11)));
        for (Quest q : result) {
           assertThat(new LocalDate(q.getEndDate(), DateTimeZone.UTC), is(greaterThanOrEqualTo(middleOfMonth)));
        }
    }

    @Test
    public void scheduleFlexibleMonthlyQuestAtTheMiddleOfMonthWithPreferredDays() {
        LocalDate middleMonday = LocalDate.now().dayOfMonth().withMinimumValue().plusDays(15).dayOfWeek().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(middleMonday));
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(middleMonday));
        assertThat(result.size(), is(lessThanOrEqualTo(11)));

        for (Quest q : result) {
            assertThat(new LocalDate(q.getEndDate(), DateTimeZone.UTC), is(greaterThanOrEqualTo(middleMonday)));
        }
    }

    @Test
    public void scheduleFlexibleMonthlyQuestWithTimesADay() {
        LocalDate startOfMonth = LocalDate.now().dayOfMonth().withMinimumValue();
        RepeatingQuest rq = createRepeatingQuest();
        rq.setTimesADay(3);
        Recurrence recurrence = Recurrence.create();
        recurrence.setRecurrenceType(Recurrence.RepeatType.MONTHLY);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setDtstartDate(toStartOfDayUTC(startOfMonth));
        recurrence.setFlexibleCount(15);
        rq.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(rq, toStartOfDayUTC(startOfMonth));
        assertThat(result.size(), is(15 * 3));
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
}