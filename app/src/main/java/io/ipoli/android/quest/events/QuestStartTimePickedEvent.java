package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class QuestStartTimePickedEvent {
    public final String mode;

    public QuestStartTimePickedEvent(String mode) {
        this.mode = mode;
    }
}
