package io.ipoli.android.quest.events;

import io.ipoli.android.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class UndoCompleteQuestEvent {
    public final Quest quest;

    public UndoCompleteQuestEvent(Quest quest) {
        this.quest = quest;
    }
}
