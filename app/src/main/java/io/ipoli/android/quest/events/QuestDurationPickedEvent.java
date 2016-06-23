package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class QuestDurationPickedEvent {
    public final String mode;

    public QuestDurationPickedEvent(String mode) {
        this.mode = mode;
    }
}
