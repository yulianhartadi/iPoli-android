package io.ipoli.android.app.scheduling.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/27/16.
 */
public class FindSlotsRequest {
    private Map<String, Object> data;

    public FindSlotsRequest(List<Task> scheduledTasks, Task taskToSchedule) {
        data = new HashMap<>();
        data.put("scheduled_tasks", scheduledTasks);
        data.put("task", taskToSchedule);
    }
}
