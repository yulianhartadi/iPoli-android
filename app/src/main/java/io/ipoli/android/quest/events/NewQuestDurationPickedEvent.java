package io.ipoli.android.quest.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/17.
 */
public class NewQuestDurationPickedEvent {
    public final int duration;

    public NewQuestDurationPickedEvent(int duration) {

        this.duration = duration;
    }
}
