package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/15/17.
 */
public class QuestPriorityPickedEvent {
    public final int priority;

    public QuestPriorityPickedEvent(int priority) {
        this.priority = priority;
    }
}
