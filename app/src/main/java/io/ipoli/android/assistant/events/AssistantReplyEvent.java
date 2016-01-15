package io.ipoli.android.assistant.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class AssistantReplyEvent {

    public final String message;

    public AssistantReplyEvent(String message) {
        this.message = message;
    }
}
