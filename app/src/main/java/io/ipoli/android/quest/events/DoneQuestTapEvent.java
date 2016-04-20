package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class DoneQuestTapEvent {
    public final Quest quest;

    public DoneQuestTapEvent(Quest quest) {
        this.quest = quest;
    }
}
