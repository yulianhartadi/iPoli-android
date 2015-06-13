package com.curiousily.ipoli.assistant.io;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.assistant.OutputHandler;
import com.curiousily.ipoli.assistant.io.event.NewMessageEvent;
import com.curiousily.ipoli.ui.events.ChangeInputEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class GuiOutputHandler implements OutputHandler {

    @Override
    public void showResponse(String response) {
        post(new NewMessageEvent(response, ChangeInputEvent.Author.iPoli));
    }

    @Override
    public void showQuery(String query) {
        post(new NewMessageEvent(query, ChangeInputEvent.Author.User));
    }

    @Override
    public void shutdown() {
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }
}
