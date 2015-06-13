package com.curiousily.ipoli.assistant;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.io.event.NewAnswerEvent;
import com.curiousily.ipoli.io.event.NewQueryEvent;
import com.squareup.otto.Subscribe;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class Assistant {

    private ElizaChat chat;

    public Assistant() {
        EventBus.get().register(this);
        chat = new ElizaChat();
    }

    @Subscribe
    public void onNewQuery(NewQueryEvent e) {
        String response = chat.respond(e.getQuery());
        EventBus.get().post(new NewAnswerEvent(response));
    }
}
