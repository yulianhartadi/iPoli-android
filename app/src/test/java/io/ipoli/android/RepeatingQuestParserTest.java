package io.ipoli.android;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.WeekDay;

import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.data.RepeatingQuest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */

public class RepeatingQuestParserTest {

    private static QuestParser questParser;
    private static PrettyTimeParser parser;

    @BeforeClass
    public static void setUp() {
        parser = new PrettyTimeParser();
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
    public void parseWithTimesPerDay() {
        RepeatingQuest rq = parse("Workout every day 4 times per day");
        assertEquals("Workout", rq.getName());
        assertThat(rq.getRecurrence().getTimesPerDay(), is(4));
    }

    @Test
    public void parseRepeatEveryDay() {
        RepeatingQuest rq = parse("Workout every day");
        assertEquals("Workout", rq.getName());
        Recur recur = new Recur(Recur.DAILY, null);
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
}
