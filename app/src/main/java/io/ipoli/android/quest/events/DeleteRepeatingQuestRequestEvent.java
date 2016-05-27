package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/16.
 */
public class DeleteRepeatingQuestRequestEvent {
    public final RepeatingQuest repeatingQuest;
    public final int position;

    public DeleteRepeatingQuestRequestEvent(RepeatingQuest repeatingQuest, int position) {
        this.repeatingQuest = repeatingQuest;
        this.position = position;
    }
}
