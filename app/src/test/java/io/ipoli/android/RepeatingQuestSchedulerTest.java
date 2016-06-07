package io.ipoli.android;

import android.support.annotation.NonNull;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;

import java.util.Date;
import java.util.List;

import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/6/16.
 */
public class RepeatingQuestSchedulerTest {

    private static RepeatingQuestScheduler repeatingQuestScheduler;

    @BeforeClass
    public static void setUp() {
        repeatingQuestScheduler = new RepeatingQuestScheduler();
    }

    @Test
    public void createRepeatingQuestStartingMonday() {
        Date monday = LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(monday);
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, monday);
        assertThat(result.size(), is(3));
    }

    @Test
    public void createRepeatingQuestStartingTuesday() {
        Date tuesday = LocalDate.now().withDayOfWeek(DateTimeConstants.TUESDAY).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(tuesday);
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, tuesday);
        assertThat(result.size(), is(2));
    }

    @Test
    public void scheduleRepeatingQuestCreatedLastFriday() {
        Date lastFriday = LocalDate.now().withDayOfWeek(DateTimeConstants.FRIDAY).minusDays(7).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(lastFriday);
        repeatingQuest.setRecurrence(recurrence);

        Date monday = LocalDate.now().dayOfWeek().withMinimumValue().toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, monday);
        assertThat(result.size(), is(3));
    }

    @Test
    public void doNotScheduleAlreadyScheduledQuests() {
        Date monday = LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = createWeeklyRecurrence(monday);
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, monday);
        result = repeatingQuestScheduler.schedule(repeatingQuest, monday, result);
        assertThat(result.size(), is(0));
    }

    @NonNull
    private Recurrence createWeeklyRecurrence(Date start) {
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(start);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString(), Recurrence.RecurrenceType.WEEKLY);
        return recurrence;
    }

    @Test
    public void scheduleDailyRepeatingQuest() {
        Date today = LocalDate.now().toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(today);
        Recur recur = new Recur(Recur.DAILY, null);
        recurrence.setRrule(recur.toString(), Recurrence.RecurrenceType.WEEKLY);
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, today);
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleMonthlyRepeatingQuestAtStartOfMonth() {
        Date startOfMonth = LocalDate.now().withDayOfMonth(1).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(startOfMonth);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(13);
        recurrence.setRrule(recur.toString(), Recurrence.RecurrenceType.WEEKLY);
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, startOfMonth);
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleMonthlyRepeatingQuestAfterMiddleOfMonth() {
        Date midOfMonth = LocalDate.now().withDayOfMonth(15).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(midOfMonth);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(13);
        recurrence.setRrule(recur.toString(), Recurrence.RecurrenceType.WEEKLY);
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, midOfMonth);
        assertThat(result.size(), is(0));
    }

    @Test
    public void scheduleYearlyRepeatingQuestAtStartOfYear() {
        Date startOfYear = LocalDate.now().withDayOfYear(1).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(startOfYear);
        Recur recur = new Recur(Recur.YEARLY, null);
        recur.getYearDayList().add(10);
        recur.getYearDayList().add(20);
        recurrence.setRrule(recur.toString(), Recurrence.RecurrenceType.WEEKLY);
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, startOfYear);
        assertThat(result.size(), is(2));
    }

    @Test
    public void scheduleYearlyRepeatingQuestAfterMiddleOfJanuary() {
        Date startOfYear = LocalDate.now().withDayOfYear(15).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(startOfYear);
        Recur recur = new Recur(Recur.YEARLY, null);
        recur.getYearDayList().add(10);
        recur.getYearDayList().add(20);
        recurrence.setRrule(recur.toString(), Recurrence.RecurrenceType.WEEKLY);
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, startOfYear);
        assertThat(result.size(), is(1));
    }

    @NonNull
    private RepeatingQuest createRepeatingQuest() {
        RepeatingQuest repeatingQuest = new RepeatingQuest("Test");
        RepeatingQuest.setContext(repeatingQuest, QuestContext.CHORES);
        return repeatingQuest;
    }
}
