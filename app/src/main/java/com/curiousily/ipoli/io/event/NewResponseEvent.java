package com.curiousily.ipoli.io.event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class NewResponseEvent {
    private final String response;

    public NewResponseEvent(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
