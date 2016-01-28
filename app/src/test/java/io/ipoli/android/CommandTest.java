package io.ipoli.android;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import io.ipoli.android.app.services.Command;
import io.ipoli.android.app.utils.DateUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/19/16.
 */
public class CommandTest {

    @Test
    public void addQuest() {
        Command cmd = Command.parseText("add quest read book");
        assertTrue(cmd == Command.ADD_QUEST);
        assertEquals("read book", cmd.getParameters().get("name"));
    }

    @Test
    public void addQuestCaseInsensitive() {
        Command cmd = Command.parseText("Add QueSt Read book");
        assertTrue(cmd == Command.ADD_QUEST);
        assertEquals("Read book", cmd.getParameters().get("name"));
    }

    @Test
    public void addTodayQuestWithMinuteDuration() {
        Command cmd = Command.parseText("add today quest read book for 30 minutes");
        assertTrue(cmd == Command.ADD_TODAY_QUEST);
        assertEquals("read book", cmd.getParameters().get("name"));
        assertEquals(30, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestWithMinuteDuration() {
        Command cmd = Command.parseText("add quest read book for 30 minutes");
        assertTrue(cmd == Command.ADD_QUEST);
        assertEquals("read book", cmd.getParameters().get("name"));
        assertEquals(30, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestWithHourDuration() {
        Command cmd = Command.parseText("add quest read book for 1 hour");
        assertTrue(cmd == Command.ADD_QUEST);
        assertEquals("read book", cmd.getParameters().get("name"));
        assertEquals(60, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestWithShortHourDuration() {
        Command cmd = Command.parseText("add quest read book for 1 h");
        assertTrue(cmd == Command.ADD_QUEST);
        assertEquals("read book", cmd.getParameters().get("name"));
        assertEquals(60, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestWithAdditionalDuration() {
        Command cmd = Command.parseText("add quest read book for mom and 3 grandmothers for 3 h and 4 m");
        assertTrue(cmd == Command.ADD_QUEST);
        assertEquals("read book for mom and 3 grandmothers", cmd.getParameters().get("name"));
        assertEquals(184, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestWithStartTime() {
        Command cmd = Command.parseText("add quest Workout at 10:00");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
    }

    @Test
    public void addQuestWith2DigitStartTime() {
        Command cmd = Command.parseText("add quest Workout at 10 am");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
    }

    @Test
    public void addQuestWith4DigitStartTime() {
        Command cmd = Command.parseText("add quest Workout at 10:00");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
    }

    @Test
    public void addQuestWithPmStartTime() {
        Command cmd = Command.parseText("add quest Workout at 10pm");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 22);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
    }

    @Test
    public void addQuestWithDotStartTime() {
        Command cmd = Command.parseText("add quest Workout at 10.30pm");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 22);
        today.set(Calendar.MINUTE, 30);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
    }

    @Test
    public void addQuestWithStartTimeAndDuration() {
        Command cmd = Command.parseText("add quest Workout for 1h at 10:00");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
        assertEquals(60, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestWithReversedStartTimeAndDuration() {
        Command cmd = Command.parseText("add quest Workout at 10:00 for 1h");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
        assertEquals(60, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestWithHourAndMinuteDurationAndStartTime() {
        Command cmd = Command.parseText("add quest Workout for 1h and 10 minutes at 10:00");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
        assertEquals(70, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestWithReversedHourAndMinuteDurationAndStartTime() {
        Command cmd = Command.parseText("add quest Workout at 10:00 for 1h and 10 minutes");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar today = DateUtils.getTodayAtMidnight();
        today.set(Calendar.HOUR_OF_DAY, 10);
        assertEquals(today.getTime(), cmd.getParameters().get("startTime"));
        assertEquals(70, cmd.getParameters().get("duration"));
    }

    @Test
    public void addQuestDue21stNextMonth() {
        Command cmd = Command.parseText("add quest Workout on 21st next month");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar expected = Calendar.getInstance();
        expected.add(Calendar.MONTH, 1);
        expected.set(Calendar.DAY_OF_MONTH, 21);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(expected.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestDue21stOfJuly() {
        Command cmd = Command.parseText("add quest Workout on 21st of July");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar expected = Calendar.getInstance();
        expected.set(Calendar.MONTH, Calendar.JULY);
        expected.set(Calendar.DAY_OF_MONTH, 21);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(expected.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestDue15Feb() {
        Command cmd = Command.parseText("add quest Workout 15 Feb");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar expected = Calendar.getInstance();
        expected.set(Calendar.MONTH, Calendar.FEBRUARY);
        expected.set(Calendar.DAY_OF_MONTH, 15);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(expected.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestForTomorrow() {
        Command cmd = Command.parseText("add quest Workout tomorrow");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(tomorrow.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestAfter3Days() {
        Command cmd = Command.parseText("add quest Workout after 3 days");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 3);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(tomorrow.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestInTwoMonths() {
        Command cmd = Command.parseText("add quest Workout in two month");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar expected = Calendar.getInstance();
        expected.add(Calendar.MONTH, 2);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(expected.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestNextFriday() {
        Command cmd = Command.parseText("add quest Workout next Friday");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar expected = getNextDayOfWeek(Calendar.FRIDAY);
        expected.add(Calendar.DAY_OF_WEEK, 7);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(expected.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestThisFriday() {
        Command cmd = Command.parseText("add quest Workout this Friday");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar expected = getNextDayOfWeek(Calendar.FRIDAY);

        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(expected.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuest3DaysFromNow() {
        Command cmd = Command.parseText("add quest Workout 3 days from now");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 3);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(tomorrow.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestTenDayFromNow() {
        Command cmd = Command.parseText("add quest Workout ten day from now");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 10);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(tomorrow.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuest1MonthFromNow() {
        Command cmd = Command.parseText("add quest Workout 1 month from now");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.MONTH, 1);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(tomorrow.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestFourYearsFromNow() {
        Command cmd = Command.parseText("add quest Workout 4 years from now");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.YEAR, 4);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(tomorrow.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void addQuestOn21st() {
        Command cmd = Command.parseText("add quest Workout on 21st");
        assertTrue(cmd == Command.ADD_QUEST);
        assertThat(cmd.getParameters().get("name").toString(), is("Workout"));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(Calendar.DAY_OF_MONTH, 21);
        Date due = (Date) cmd.getParameters().get("due");
        Calendar dueC = Calendar.getInstance();
        dueC.setTime(due);
        assertTrue(tomorrow.get(Calendar.DAY_OF_YEAR) == dueC.get(Calendar.DAY_OF_YEAR));
    }

    public Calendar getNextDayOfWeek(int dayOfWeek){
        Calendar today = Calendar.getInstance();
        int currDayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int daysUntilNextWeekOfDay = dayOfWeek - currDayOfWeek;
        Calendar nextDayOfWeek = (Calendar)today.clone();
        nextDayOfWeek.add(Calendar.DAY_OF_WEEK, daysUntilNextWeekOfDay);
        return nextDayOfWeek;
    }
}
