package io.ipoli.android;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Calendar;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.QuestParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class QuestParserTest {

    private static QuestParser questParser;

    @BeforeClass
    public static void setUp() {
        PrettyTimeParser parser = new PrettyTimeParser();
        questParser = new QuestParser(parser);
    }

    private Quest parse(String text) {
        return questParser.parse(text);
    }

    @Test
    public void addQuest() {
        Quest q = parse("read book");
        assertEquals("read book", q.getName());
    }

    @Test
    public void addQuestCaseInsensitive() {
        Quest q = parse("Read book");
        assertEquals("Read book", q.getName());
    }

    @Test
    public void addTodayQuestWithMinuteDuration() {
        Quest q = parse("read book for 30 minutes today");
        assertEquals("read book", q.getName());
        assertEquals(30, q.getDuration());
        assertTrue(DateUtils.isToday(q.getEndDate()));
    }

    @Test
    public void addQuestWithMinuteDuration() {
        Quest q = parse("read book for 30 minutes");
        assertEquals("read book", q.getName());
        assertEquals(30, q.getDuration());
    }

    @Test
    public void addQuestWithHourDuration() {
        Quest q = parse("read book for 1 hour");
        assertEquals("read book", q.getName());
        assertEquals(60, q.getDuration());
    }

    @Test
    public void addQuestWithShortHourDuration() {
        Quest q = parse("read book for 1 h");
        assertEquals("read book", q.getName());
        assertEquals(60, q.getDuration());
    }

    @Test
    public void addQuestWithAdditionalDuration() {
        Quest q = parse("read book for mom and 3 grandmothers for 3 h and 4 m");
        assertEquals("read book for mom and 3 grandmothers", q.getName());
        assertEquals(184, q.getDuration());
    }

    @Test
    public void addQuestWithStartTime() {
        Quest q = parse("Workout at 10:00");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinutesAfterMidnight()));
    }

    @Test
    public void addQuestWith2DigitStartTime() {
        Quest q = parse("Workout at 10 am");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinutesAfterMidnight()));
    }

    @Test
    public void addQuestWith4DigitStartTime() {
        Quest q = parse("Workout at 10:00");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinutesAfterMidnight()));
    }

    @Test
    public void addQuestWithPmStartTime() {
        Quest q = parse("Workout at 10pm");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(22).toMinutesAfterMidnight()));
    }

    @Test
    public void addQuestWithDotStartTime() {
        Quest q = parse("Workout at 10.30pm");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.at(22, 30).toMinutesAfterMidnight()));
    }

    @Test
    public void addQuestWithStartTimeAndDuration() {
        Quest q = parse("Workout for 1h at 10:00");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinutesAfterMidnight()));
        assertThat(q.getDuration(), is(60));
    }

    @Test
    public void addQuestWithReversedStartTimeAndDuration() {
        Quest q = parse("Workout at 10:00 for 1h");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinutesAfterMidnight()));
        assertThat(q.getDuration(), is(60));
    }

    @Test
    public void addQuestWithHourAndMinuteDurationAndStartTime() {
        Quest q = parse("Workout for 1h and 10 minutes at 10:00");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinutesAfterMidnight()));
        assertThat(q.getDuration(), is(70));
    }

    @Test
    public void addQuestWithReversedHourAndMinuteDurationAndStartTime() {
        Quest q = parse("Workout at 10:00 for 1h and 10 minutes");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinutesAfterMidnight()));
        assertThat(q.getDuration(), is(70));
    }

    @Test
    public void addQuestDue21stNextMonth() {
        Quest q = parse("Workout on 21st next month");
        assertThat(q.getName(), is("Workout"));
        Calendar expected = Calendar.getInstance();
        expected.add(Calendar.MONTH, 1);
        expected.set(Calendar.DAY_OF_MONTH, 21);
        assertDueDate(q, expected);
    }

    @Test
    public void addQuestDue21stOfJuly() {
        Quest q = parse("Workout on 21st of July");
        assertThat(q.getName(), is("Workout"));
        Calendar expected = Calendar.getInstance();
        expected.set(Calendar.MONTH, Calendar.JULY);
        expected.set(Calendar.DAY_OF_MONTH, 21);
        assertDueDate(q, expected);
    }

    @Test
    public void addQuestDue15Feb() {
        Quest q = parse("Workout 15 Feb");
        assertThat(q.getName(), is("Workout"));
        Calendar expected = Calendar.getInstance();
        expected.set(Calendar.MONTH, Calendar.FEBRUARY);
        expected.set(Calendar.DAY_OF_MONTH, 15);
        assertDueDate(q, expected);
    }

    @Test
    public void addQuestForTomorrow() {
        Quest q = parse("Workout tomorrow");
        assertThat(q.getName(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        assertDueDate(q, tomorrow);
    }

    @Test
    public void addQuestAfter3Days() {
        Quest q = parse("Workout after 3 days");
        assertThat(q.getName(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 3);
        assertDueDate(q, tomorrow);
    }

    @Test
    public void addQuestInTwoMonths() {
        Quest q = parse("Workout in two month");
        assertThat(q.getName(), is("Workout"));
        Calendar expected = Calendar.getInstance();
        expected.add(Calendar.MONTH, 2);
        assertDueDate(q, expected);
    }

    @Test
    public void addQuestNextFriday() {
        Quest q = parse("Workout next Friday");
        assertThat(q.getName(), is("Workout"));
        Calendar expected = getNextDayOfWeek(Calendar.FRIDAY);
        assertDueDate(q, expected);
    }

    @Test
    public void addQuestThisFriday() {
        Quest q = parse("Workout this Friday");
        assertThat(q.getName(), is("Workout"));
        Calendar expected = getNextDayOfWeek(Calendar.FRIDAY);
        assertDueDate(q, expected);
    }

    @Test
    public void addQuest3DaysFromNow() {
        Quest q = parse("Workout 3 days from now");
        assertThat(q.getName(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 3);
        assertDueDate(q, tomorrow);
    }

    @Test
    public void addQuestTenDayFromNow() {
        Quest q = parse("Workout ten day from now");
        assertThat(q.getName(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 10);
        assertDueDate(q, tomorrow);
    }

    @Test
    public void addQuest1MonthFromNow() {
        Quest q = parse("Workout 1 month from now");
        assertThat(q.getName(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.MONTH, 1);
        assertDueDate(q, tomorrow);
    }

    @Test
    public void addQuestFourYearsFromNow() {
        Quest q = parse("Workout 4 years from now");
        assertThat(q.getName(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.YEAR, 4);
        assertDueDate(q, tomorrow);
    }

    @Test
    public void addQuestOn21st() {
        Quest q = parse("Workout on 21st");
        assertThat(q.getName(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(Calendar.DAY_OF_MONTH, 21);
        assertDueDate(q, tomorrow);
    }

    public Calendar getNextDayOfWeek(int dayOfWeek) {
        Calendar today = Calendar.getInstance();
        int currDayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int daysUntilNextWeekOfDay = 7;
        if (currDayOfWeek > dayOfWeek) {
            daysUntilNextWeekOfDay = 7 - currDayOfWeek + 1;
        } else if (currDayOfWeek < dayOfWeek) {
            daysUntilNextWeekOfDay = dayOfWeek - currDayOfWeek;
        }
        Calendar nextDayOfWeek = (Calendar) today.clone();
        nextDayOfWeek.add(Calendar.DAY_OF_YEAR, daysUntilNextWeekOfDay);
        return nextDayOfWeek;
    }

    private void assertDueDate(Quest q, Calendar expected) {
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(q.getEndDate());
        assertTrue(expected.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }
}
