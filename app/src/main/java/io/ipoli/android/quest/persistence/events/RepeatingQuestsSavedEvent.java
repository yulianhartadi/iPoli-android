package io.ipoli.android.quest.persistence.events;

import java.util.List;

import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/17/16.
 */
public class RepeatingQuestsSavedEvent {
    public final List<RepeatingQuest> objects;

    public RepeatingQuestsSavedEvent(List<RepeatingQuest> objects) {

        this.objects = objects;
    }
}
