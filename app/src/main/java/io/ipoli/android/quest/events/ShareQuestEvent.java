package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/29/16.
 */
public class ShareQuestEvent {

    public final Quest quest;

    public ShareQuestEvent(Quest quest) {

        this.quest = quest;
    }
}
