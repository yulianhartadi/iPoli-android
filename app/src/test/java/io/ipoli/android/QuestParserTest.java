package io.ipoli.android;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ipoli.android.app.parsers.DateTimeParser;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.data.Quest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */

public class QuestParserTest {

    private static QuestParser questParser;
    private static DateTimeParser parser;
    private static LocalDate today;

    @BeforeClass
    public static void setUp() {
        parser = new DateTimeParser();
        questParser = new QuestParser(parser);
        today = LocalDate.now();
    }

    private Quest parse(String text) {
        return questParser.parseQuest(text);
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
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinuteOfDay()));
    }

    @Test
    public void addQuestWith2DigitStartTime() {
        Quest q = parse("Workout at 10 am");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinuteOfDay()));
    }

    @Test
    public void addQuestWith4DigitStartTime() {
        Quest q = parse("Workout at 10:00");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinuteOfDay()));
    }

    @Test
    public void addQuestWithPmStartTime() {
        Quest q = parse("Workout at 10pm");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(22).toMinuteOfDay()));
    }

    @Test
    public void addQuestWithDotStartTime() {
        Quest q = parse("Workout at 10.30pm");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.at(22, 30).toMinuteOfDay()));
    }

    @Test
    public void addQuestWithStartTimeAndDuration() {
        Quest q = parse("Workout for 1h at 10:00");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinuteOfDay()));
        assertThat(q.getDuration(), is(60));
    }

    @Test
    public void addQuestWithReversedStartTimeAndDuration() {
        Quest q = parse("Workout at 10:00 for 1h");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinuteOfDay()));
        assertThat(q.getDuration(), is(60));
    }

    @Test
    public void addQuestWithHourAndMinuteDurationAndStartTime() {
        Quest q = parse("Workout for 1h and 10 minutes at 10:00");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinuteOfDay()));
        assertThat(q.getDuration(), is(70));
    }

    @Test
    public void addQuestWithReversedHourAndMinuteDurationAndStartTime() {
        Quest q = parse("Workout at 10:00 for 1h and 10 minutes");
        assertThat(q.getName(), is("Workout"));
        assertThat(q.getStartMinute(), is(Time.atHours(10).toMinuteOfDay()));
        assertThat(q.getDuration(), is(70));
    }

    @Test
    public void addQuestDueToday() {
        Quest q = parse("Workout today");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today);
    }

    @Test
    public void addQuestDueTomorrow() {
        Quest q = parse("Workout todays tomorrow");
        assertThat(q.getName(), is("Workout todays"));
        assertDueDate(q, today.plusDays(1));
    }

    @Test
    @Ignore
    public void addQuestDue21stNextMonth() {
        Quest q = parse("Workout on 21st next month");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.plusMonths(1).withDayOfMonth(21));
    }

    @Test
    @Ignore
    public void addQuestDue21stOfJuly() {
        Quest q = parse("Workout on 21st of July");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.with(Month.JULY).withDayOfMonth(21));
    }

    @Test
    public void addQuestDue15Feb() {
        Quest q = parse("Workout on 15 Feb");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.with(Month.FEBRUARY).withDayOfMonth(15));
    }

    @Test
    public void addQuestForTomorrow() {
        Quest q = parse("Workout tomorrow");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.plusDays(1));
    }

    @Test
    public void addQuestAfter3Days() {
        Quest q = parse("Workout after 3 days");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.plusDays(3));
    }

    @Test
    public void addQuestInTwoMonths() {
        Quest q = parse("Workout in two month");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.plusMonths(2));
    }

    @Test
    @Ignore("PrettyTime not parsing next date properly")
    public void addQuestNextFriday() {
        Quest q = parse("Workout next Friday");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)));
    }

    @Test
    public void addQuestThisFridayWhenItIsThursday() {
        Date currentDate = DateUtils.toStartOfDay(today.with(DayOfWeek.THURSDAY));
        QuestParser questParser = new QuestParser(parser, currentDate);
        Quest q = questParser.parseQuest("Workout this Friday");
        assertThat(q.getName(), is("Workout"));
        LocalDate thisFriday = today.with(DayOfWeek.FRIDAY);
        assertDueDate(q, thisFriday);
    }

    @Test
    public void addQuest3DaysFromNow() {
        Quest q = parse("Workout 3 days from now");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.plusDays(3));
    }

    @Test
    public void addQuestThreeDayFromNow() {
        Quest q = parse("Workout three day from now");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.plusDays(3));
    }

    @Test
    public void addQuest1MonthFromNow() {
        Quest q = parse("Workout 1 month from now");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.plusMonths(1));
    }

    @Test
    public void addQuestFourYearsFromNow() {
        Quest q = parse("Workout 4 years from now");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.plusYears(4));
    }

    @Test
    public void addQuestOn21st() {
        Quest q = parse("Workout on 21st");
        assertThat(q.getName(), is("Workout"));
        assertDueDate(q, today.withDayOfMonth(21));
    }

    @Test
    public void testHitEndDueDate() {
        String DUE_MONTH_PATTERN = "(?:^|\\s)on\\s(\\d){1,2}(\\s)?(st|th)?\\s(of\\s)?(next month|this month|January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}(?:$|\\s)";
        Pattern p = Pattern.compile(DUE_MONTH_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("on 12");
        m.matches();
        assertTrue(m.hitEnd());
    }

    @Test
    public void testHitEndToday() {
        String PATTERN = "(?:^|\\s)(today|tomorrow)(?:$|\\s)";
        Pattern p = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("today ");
        m.matches();
        assertTrue(!m.hitEnd());
    }

    @Test
    public void testHitEndTimesADay() {
        String PATTERN = "(?:^|\\s)(\\d+)\\stimes(?:\\sa\\sday)?(?:$|\\s)";
        Pattern p = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(" 4 times a");
        m.matches();
        assertTrue(m.hitEnd());
    }

    private void assertDueDate(Quest q, LocalDate expected) {
        assertTrue(q.getEndDate().isEqual(expected));
    }
}