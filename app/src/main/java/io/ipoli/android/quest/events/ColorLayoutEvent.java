package io.ipoli.android.quest.events;

import io.ipoli.android.quest.QuestContext;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/10/16.
 */
public class ColorLayoutEvent {
    public final QuestContext questContext;

    public ColorLayoutEvent(QuestContext questContext) {
        this.questContext = questContext;
    }
}
