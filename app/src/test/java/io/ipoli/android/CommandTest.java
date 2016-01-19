package io.ipoli.android;

import org.junit.Test;

import java.util.Calendar;

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
}
