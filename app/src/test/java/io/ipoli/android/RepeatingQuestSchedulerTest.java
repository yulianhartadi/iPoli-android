package io.ipoli.android;

import android.support.annotation.NonNull;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;
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
        recurrence.setDtstart(start);
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
        recurrence.setDtstart(today);
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
        recurrence.setDtstart(startOfMonth);
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
        recurrence.setDtstart(midOfMonth);
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(13);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, midOfMonth);
        assertThat(result.size(), is(0));
    }

    @Test
    @Ignore
    public void scheduleYearlyRepeatingQuestAtStartOfYear() {
        Date startOfYear = toStartOfDayUTC(LocalDate.now().withDayOfYear(1));
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstart(startOfYear);
        Recur recur = new Recur(Recur.YEARLY, null);
        recur.getYearDayList().add(10);
        recur.getYearDayList().add(20);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, startOfYear);
        assertThat(result.size(), is(2));
    }

    @Test
    @Ignore
    public void scheduleYearlyRepeatingQuestAfterMiddleOfJanuary() {
        Date startOfYear = toStartOfDayUTC(LocalDate.now().withDayOfYear(15));
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstart(startOfYear);
        Recur recur = new Recur(Recur.YEARLY, null);
        recur.getYearDayList().add(10);
        recur.getYearDayList().add(20);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, startOfYear);
        assertThat(result.size(), is(1));
    }

    @Test
    public void scheduleQuestForSingleDay() {
        Date today = toStartOfDayUTC(LocalDate.now());
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = Recurrence.create();
        recurrence.setDtstart(today);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        recur.getDayList().add(WeekDay.SA);
        recur.getDayList().add(WeekDay.SU);
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
        recurrence.setDtstart(startOfWeek);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        recur.getDayList().add(WeekDay.SA);
        recur.getDayList().add(WeekDay.SU);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.scheduleForDateRange(repeatingQuest, startOfWeek, endOfWeek);
        assertThat(result.size(), is(7));
    }

    @Test
    public void scheduleRepeatingQuestWithTimesPerDay() {
        Date today = toStartOfDayUTC(LocalDate.now());
        RepeatingQuest repeatingQuest = createRepeatingQuest();
        Recurrence recurrence = new Recurrence(4);
        recurrence.setDtstart(today);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, today);
        assertThat(result.size(), is(4));
    }

    @NonNull
    private RepeatingQuest createRepeatingQuest() {
        RepeatingQuest repeatingQuest = new RepeatingQuest("Test");
        RepeatingQuest.setContext(repeatingQuest, QuestContext.CHORES);
        return repeatingQuest;
    }
}
