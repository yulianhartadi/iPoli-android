package io.ipoli.android.quest.events;

import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/27/16.
 */
public class ScheduleQuestRequestEvent {
    public final UnscheduledQuestViewModel viewModel;

    public ScheduleQuestRequestEvent(UnscheduledQuestViewModel viewModel) {
        this.viewModel = viewModel;
    }
}
