package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.utils.TimePreference;

import static io.ipoli.android.app.utils.Time.h2Min;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class MorningConstraint extends SoftConstraint {

    public static final int MORNING_START = h2Min(6);
    public static final int MORNING_END = h2Min(12);

    public MorningConstraint() {
        super(MORNING_START, MORNING_END);
    }

    @Override
    public boolean shouldApply(Task task) {
        return task.getStartTimePreference() == TimePreference.MORNING;
    }

}
