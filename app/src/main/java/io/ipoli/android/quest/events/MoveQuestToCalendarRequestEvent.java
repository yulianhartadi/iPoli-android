package io.ipoli.android.quest.events;

import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class MoveQuestToCalendarRequestEvent {
    public final UnscheduledQuestViewModel viewModel;
    public final int position;

    public MoveQuestToCalendarRequestEvent(UnscheduledQuestViewModel viewModel, int position) {
        this.viewModel = viewModel;
        this.position = position;
    }
}
