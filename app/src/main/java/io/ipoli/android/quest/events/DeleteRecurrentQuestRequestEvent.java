package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.RecurrentQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/16.
 */
public class DeleteRecurrentQuestRequestEvent {
    public final RecurrentQuest recurrentQuest;
    public final int position;

    public DeleteRecurrentQuestRequestEvent(RecurrentQuest recurrentQuest, int position) {
        this.recurrentQuest = recurrentQuest;
        this.position = position;
    }
}
