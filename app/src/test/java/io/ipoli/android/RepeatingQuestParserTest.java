package io.ipoli.android;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;

import java.util.Calendar;

import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.data.RepeatingQuest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */

public class RepeatingQuestParserTest {

    private static QuestParser questParser;

    @BeforeClass
    public static void setUp() {
        PrettyTimeParser parser = new PrettyTimeParser();
        questParser = new QuestParser(parser);
    }

    private RepeatingQuest parse(String text) {
        return questParser.parseRepeatingQuest(text);
    }

    @Test
    public void parseWitName() {
        RepeatingQuest rq = parse("Workout every day for 1h");
        assertEquals("Workout", rq.getName());
    }

    @Test
    public void parseWithDuration() {
        RepeatingQuest rq = parse("Workout every day for 1h");
        assertEquals("Workout", rq.getName());
        assertEquals(60, rq.getDuration());
    }

    @Test
    public void parseWithStartTime() {
        RepeatingQuest rq = parse("Workout every day at 10:00");
        assertEquals("Workout", rq.getName());
        assertThat(rq.getStartMinute(), is(Time.atHours(10).toMinutesAfterMidnight()));
    }

    @Test
    public void parseWithTimesADay() {
        RepeatingQuest rq = parse("Workout every day 4 times a day");
        assertEquals("Workout", rq.getName());
        assertThat(rq.getRecurrence().getTimesPerDay(), is(4));
    }

    @Test
    public void parseWithDateAndTimesADay() {
        RepeatingQuest rq = parse("Workout today 4 times a day");
        assertEquals("Workout", rq.getName());
        assertThat(rq.getRecurrence().getTimesPerDay(), is(4));
        assertStartDate(rq, Calendar.getInstance());
    }

    @Test
    public void parseWithDateAndTimesADayAndDuration() {
        RepeatingQuest rq = parse("Workout tomorrow 2 times a day for 1h");
        assertEquals("Workout", rq.getName());
        assertThat(rq.getRecurrence().getTimesPerDay(), is(2));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        assertStartDate(rq, cal);
        assertEquals(60, rq.getDuration());
    }

    @Test
    public void parseRepeatEveryDay() {
        RepeatingQuest rq = parse("Workout every day");
        assertEquals("Workout", rq.getName());
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        recur.getDayList().add(WeekDay.SA);
        recur.getDayList().add(WeekDay.SU);
        assertThat(rq.getRecurrence().getRrule(), is(recur.toString()));
    }

    @Test
    public void parseRepeatEveryWeekday() {
        RepeatingQuest rq = parse("Workout every Mon and Wed");
        assertEquals("Workout", rq.getName());
        Recur recur = new Recur(Recur.WEEKLY, null);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.WE);
        assertThat(rq.getRecurrence().getRrule(), is(recur.toString()));
    }

    @Test
    public void parseRepeatEveryDayOfTheMonth() {
        RepeatingQuest rq = parse("Workout every 21st of the month");
        assertEquals("Workout", rq.getName());
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(21);
        assertThat(rq.getRecurrence().getRrule(), is(recur.toString()));
    }

    @Test
    public void parseRepeatOnDayEveryMonth() {
        RepeatingQuest rq = parse("Workout on 21st every month");
        assertEquals("Workout", rq.getName());
        Recur recur = new Recur(Recur.MONTHLY, null);
        recur.getMonthDayList().add(21);
        assertThat(rq.getRecurrence().getRrule(), is(recur.toString()));
    }

    private void assertStartDate(RepeatingQuest rq, Calendar expected) {
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(rq.getRecurrence().getDtstart());
        assertTrue(expected.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }
}
