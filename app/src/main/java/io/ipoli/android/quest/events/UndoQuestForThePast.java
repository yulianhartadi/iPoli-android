package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/16.
 */
public class UndoQuestForThePast {
    public final Quest quest;

    public UndoQuestForThePast(Quest quest) {
        this.quest = quest;
    }
}
