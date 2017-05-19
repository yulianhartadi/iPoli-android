package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/24/17.
 */
public class AfternoonConstraint extends SoftConstraint {

    public static final int AFTERNOON_START = Time.h2Min(13);
    public static final int AFTERNOON_END = Time.h2Min(17);

    public AfternoonConstraint(int slotDuration) {
        super(AFTERNOON_START, AFTERNOON_END, slotDuration);
    }

    @Override
    public boolean shouldApply(Task task) {
        return task.getStartTimePreference() == TimePreference.AFTERNOON;
    }
}
