package com.curiousily.ipoli.assistant.intents.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class IntentProcessedEvent {
    private final String response;

    public IntentProcessedEvent(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
