package com.curiousily.ipoli.assistant.handlers.intents;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/19/15.
 */
public class ChatIntent implements Intent {
    private String response;

    public ChatIntent(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
