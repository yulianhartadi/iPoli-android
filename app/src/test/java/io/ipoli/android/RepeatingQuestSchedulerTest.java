package io.ipoli.android;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;

import java.util.ArrayList;
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
        RepeatingQuest repeatingQuest = new RepeatingQuest("Test");
        RepeatingQuest.setContext(repeatingQuest, QuestContext.CHORES);
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(monday);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, monday, new ArrayList<>());
        assertThat(result.size(), is(3));
    }

    @Test
    public void createRepeatingQuestStartingTuesday() {
        Date tuesday = LocalDate.now().withDayOfWeek(DateTimeConstants.TUESDAY).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = new RepeatingQuest("Test");
        RepeatingQuest.setContext(repeatingQuest, QuestContext.CHORES);
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(tuesday);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, tuesday, new ArrayList<>());
        assertThat(result.size(), is(2));
    }

    @Test
    public void scheduleRepeatingQuestCreatedLastFriday() {
        Date lastFriday = LocalDate.now().withDayOfWeek(DateTimeConstants.FRIDAY).minusDays(7).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = new RepeatingQuest("Test");
        RepeatingQuest.setContext(repeatingQuest, QuestContext.CHORES);
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(lastFriday);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);

        Date monday = LocalDate.now().dayOfWeek().withMinimumValue().toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, monday, new ArrayList<>());
        assertThat(result.size(), is(3));
    }

    @Test
    public void doNotScheduleAlreadyScheduledQuests() {
        Date monday = LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY).toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
        RepeatingQuest repeatingQuest = new RepeatingQuest("Test");
        RepeatingQuest.setContext(repeatingQuest, QuestContext.CHORES);
        Recurrence recurrence = new Recurrence();
        recurrence.setDtstart(monday);
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.FR);
        recurrence.setRrule(recur.toString());
        repeatingQuest.setRecurrence(recurrence);

        List<Quest> result = repeatingQuestScheduler.schedule(repeatingQuest, monday, new ArrayList<>());
        result = repeatingQuestScheduler.schedule(repeatingQuest, monday, result);
        assertThat(result.size(), is(0));
    }

}
