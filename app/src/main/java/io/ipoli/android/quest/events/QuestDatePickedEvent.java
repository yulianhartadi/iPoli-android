package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class QuestDatePickedEvent {
    public final String mode;

    public QuestDatePickedEvent(String mode) {
        this.mode = mode;
    }
}
