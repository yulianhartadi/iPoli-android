package io.ipoli.android.quest.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/17.
 */
public class NewQuestPriorityPickedEvent {
    public final int priority;

    public NewQuestPriorityPickedEvent(int priority) {

        this.priority = priority;
    }
}