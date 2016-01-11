package io.ipoli.android.assistant.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/11/16.
 */
public class NewTodayQuestEvent {
    public final String name;

    public NewTodayQuestEvent(String name) {
        this.name = name;
    }
}
