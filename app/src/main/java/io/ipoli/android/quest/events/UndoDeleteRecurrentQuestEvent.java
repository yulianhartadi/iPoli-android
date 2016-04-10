package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.RecurrentQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/9/16.
 */
public class UndoDeleteRecurrentQuestEvent {
    public final RecurrentQuest recurrentQuest;

    public UndoDeleteRecurrentQuestEvent(RecurrentQuest recurrentQuest) {
        this.recurrentQuest = recurrentQuest;
    }
}
