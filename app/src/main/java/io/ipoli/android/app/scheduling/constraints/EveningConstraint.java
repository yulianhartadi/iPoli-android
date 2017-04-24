package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/24/17.
 */
public class EveningConstraint extends SoftConstraint {

    public static final int EVENING_START = Time.h2Min(18);
    public static final int EVENING_END = Time.h2Min(23);

    public EveningConstraint() {
        super(EVENING_START, EVENING_END);
    }

    @Override
    public boolean shouldApply(Task task) {
        return task.getStartTimePreference() == TimePreference.EVENING;
    }
}
