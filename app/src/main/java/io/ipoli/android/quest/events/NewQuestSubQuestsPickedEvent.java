package io.ipoli.android.quest.events;

import java.util.List;

import io.ipoli.android.quest.data.SubQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/17.
 */
public class NewQuestSubQuestsPickedEvent {
    public final List<SubQuest> subQuests;

    public NewQuestSubQuestsPickedEvent(List<SubQuest> subQuests) {
        this.subQuests = subQuests;
    }
}
