package io.ipoli.android.quest.ui.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/16.
 */
public class EditRepeatingQuestRequestEvent {
    public final RepeatingQuest repeatingQuest;
    public final EventSource source;

    public EditRepeatingQuestRequestEvent(RepeatingQuest repeatingQuest, EventSource source) {

        this.repeatingQuest = repeatingQuest;
        this.source = source;
    }
}
