package com.curiousily.ipoli.assistant.handlers.intents;

import com.google.gson.Gson;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/19/15.
 */
public class ChatIntent implements Intent {
    private String name;
    private String response;

    public ChatIntent(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public static ChatIntent from(Object intent) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(intent);
        return gson.fromJson(jsonString, ChatIntent.class);
    }
}
