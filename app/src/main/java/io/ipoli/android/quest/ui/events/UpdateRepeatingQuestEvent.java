package io.ipoli.android.quest.ui.events;

import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/16.
 */
public class UpdateRepeatingQuestEvent {
    public final RepeatingQuest repeatingQuest;

    public UpdateRepeatingQuestEvent(RepeatingQuest repeatingQuest) {
        this.repeatingQuest = repeatingQuest;
    }
}