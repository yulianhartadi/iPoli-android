package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class StopQuestTapEvent {
    public final Quest quest;

    public StopQuestTapEvent(Quest quest) {
        this.quest = quest;
    }
}
