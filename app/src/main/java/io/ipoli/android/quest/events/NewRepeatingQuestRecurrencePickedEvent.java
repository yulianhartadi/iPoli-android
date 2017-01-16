package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Recurrence;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/16/17.
 */
public class NewRepeatingQuestRecurrencePickedEvent {
    public final Recurrence recurrence;

    public NewRepeatingQuestRecurrencePickedEvent(Recurrence recurrence) {
        this.recurrence = recurrence;
    }
}
