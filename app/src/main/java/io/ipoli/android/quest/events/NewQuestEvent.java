package io.ipoli.android.quest.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class NewQuestEvent {
    public final String name;

    public NewQuestEvent(String name) {
        this.name = name;
    }
}
