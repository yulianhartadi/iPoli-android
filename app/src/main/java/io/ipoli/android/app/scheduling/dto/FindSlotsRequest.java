package io.ipoli.android.app.scheduling.dto;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/27/16.
 */
public class FindSlotsRequest {
    private List<Task> scheduledTasks;
    private Task taskToSchedule;

    public FindSlotsRequest(List<Task> scheduledTasks, Task taskToSchedule) {
        this.scheduledTasks = scheduledTasks;
        this.taskToSchedule = taskToSchedule;
    }
}
