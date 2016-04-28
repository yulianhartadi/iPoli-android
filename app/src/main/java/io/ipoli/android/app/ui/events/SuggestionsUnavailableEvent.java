package io.ipoli.android.app.ui.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class SuggestionsUnavailableEvent {
    public final Quest quest;

    public SuggestionsUnavailableEvent(Quest quest) {

        this.quest = quest;
    }
}
