package io.ipoli.android.quest.persistence.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/21/16.
 */
public class QuestDeletedEvent {

    public final String id;

    public QuestDeletedEvent(String id) {
        this.id = id;
    }
}
