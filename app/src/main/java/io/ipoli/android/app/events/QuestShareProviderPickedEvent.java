package io.ipoli.android.app.events;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/3/16.
 */
public class QuestShareProviderPickedEvent {
    public final String provider;
    public final Quest quest;

    public QuestShareProviderPickedEvent(String provider, Quest quest) {
        this.provider = provider;
        this.quest = quest;
    }
}
