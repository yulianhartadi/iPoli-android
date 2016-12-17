package io.ipoli.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.TaskScheduler;
import io.ipoli.android.app.scheduling.TimeBlock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class SchedulerTest {

    @Test
    public void shouldHave1FreeBlock() {
        TaskScheduler scheduler = new TaskScheduler(0, 1);
        List<TimeBlock> freeBlocks = scheduler.getFreeBlocks();
        assertThat(freeBlocks.size(), is(1));
    }

    @Test
    public void shouldHaveCorrectStartMinuteForTimeBlock() {
        TaskScheduler scheduler = new TaskScheduler(0, 1);
        List<TimeBlock> freeBlocks = scheduler.getFreeBlocks();
        assertThat(freeBlocks.get(0).getStartMinute(), is(0));
    }

    @Test
    public void shouldHaveCorrectEndMinuteForTimeBlock() {
        TaskScheduler scheduler = new TaskScheduler(0, 1);
        List<TimeBlock> freeBlocks = scheduler.getFreeBlocks();
        assertThat(freeBlocks.get(0).getEndMinute(), is(1 * 60));
    }

    @Test
    public void shouldHave2TimeBlocks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(10, 15));
        TaskScheduler scheduler = new TaskScheduler(0, 1, tasks);
        List<TimeBlock> freeBlocks = scheduler.getFreeBlocks();
        assertThat(freeBlocks.size(), is(2));
    }

    @Test
    public void shouldHaveFreeBlocksWithEnoughDuration() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(10, 20));
        TaskScheduler scheduler = new TaskScheduler(0, 1, tasks);
        List<TimeBlock> freeBlocks = scheduler.getFreeBlocksFor(new Task(15));
        assertThat(freeBlocks.size(), is(1));
    }
}
