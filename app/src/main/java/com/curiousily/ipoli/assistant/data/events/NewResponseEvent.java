package com.curiousily.ipoli.assistant.data.events;

import com.curiousily.ipoli.assistant.data.models.Response;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/19/15.
 */
public class NewResponseEvent {
    private final Response response;

    public NewResponseEvent(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }
}
