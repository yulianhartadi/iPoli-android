package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.RecurrentQuest;

/**
 * Created by Naughty Spirit <hi@naughtyspirit.co>
 * on 4/9/16.
 */
public class UndoDeleteRecurrentQuestEvent {
    public final RecurrentQuest recurrentQuest;

    public UndoDeleteRecurrentQuestEvent(RecurrentQuest recurrentQuest) {
        this.recurrentQuest = recurrentQuest;
    }
}
