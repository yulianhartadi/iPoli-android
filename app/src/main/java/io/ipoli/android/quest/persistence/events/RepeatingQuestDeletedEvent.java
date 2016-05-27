package io.ipoli.android.quest.persistence.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/15/16.
 */
public class RepeatingQuestDeletedEvent {

    public final String id;

    public RepeatingQuestDeletedEvent(String id) {
        this.id = id;
    }
}
