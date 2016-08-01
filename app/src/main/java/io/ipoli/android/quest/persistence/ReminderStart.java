package io.ipoli.android.quest.persistence;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/31/16.
 */
public class ReminderStart {
    public final long startTime;
    public final List<String> questIds;

    public ReminderStart(long startTime, List<String> questIds) {
        this.startTime = startTime;
        this.questIds = questIds;
    }
}
