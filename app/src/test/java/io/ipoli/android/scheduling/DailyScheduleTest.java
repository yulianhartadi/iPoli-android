package io.ipoli.android.scheduling;

import org.junit.Test;

import java.util.Collections;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.quest.data.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailyScheduleTest {

    @Test
    public void shouldHaveFreeTimeAtStart() {
        DailySchedule schedule = new DailySchedule(0, 60, 15, Collections.singletonList(new Task(30, 30, Category.PERSONAL)));
        assertTrue(schedule.isFree(0, 30));
        assertFalse(schedule.isFree(30, 60));
    }

    @Test
    public void shouldHaveFreeTimeAtEnd() {
        DailySchedule schedule = new DailySchedule(0, 60, 15, Collections.singletonList(new Task(0, 30, Category.PERSONAL)));
        assertTrue(schedule.isFree(30, 60));
        assertFalse(schedule.isFree(0, 30));
    }

    @Test
    public void shouldNotHaveFreeTimeAtSlot() {
        DailySchedule schedule = new DailySchedule(0, 60, 15, Collections.singletonList(new Task(5, 30, Category.PERSONAL)));
        assertTrue(schedule.isFree(30, 60));
        assertFalse(schedule.isFree(0, 30));
    }
}