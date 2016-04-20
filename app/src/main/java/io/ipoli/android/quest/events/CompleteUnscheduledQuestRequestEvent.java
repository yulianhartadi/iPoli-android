package io.ipoli.android.quest.events;

import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class CompleteUnscheduledQuestRequestEvent {
    public final UnscheduledQuestViewModel viewModel;

    public CompleteUnscheduledQuestRequestEvent(UnscheduledQuestViewModel viewModel) {
        this.viewModel = viewModel;
    }
}
