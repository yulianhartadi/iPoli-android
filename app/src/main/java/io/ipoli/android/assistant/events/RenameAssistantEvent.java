package io.ipoli.android.assistant.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class RenameAssistantEvent {
    public final String name;

    public RenameAssistantEvent(String name) {
        this.name = name;
    }
}
