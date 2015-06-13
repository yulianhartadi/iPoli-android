package com.curiousily.ipoli.assistant;

import android.content.Context;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.assistant.io.event.NewQueryEvent;
import com.curiousily.ipoli.assistant.io.event.NewResponseEvent;
import com.curiousily.ipoli.assistant.io.gui.ConversationPresenter;
import com.curiousily.ipoli.assistant.io.speaker.Speaker;
import com.curiousily.ipoli.assistant.io.speech.VoiceRecognizer;
import com.curiousily.ipoli.ui.events.ShutdownEvent;
import com.squareup.otto.Subscribe;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class Assistant {

    private ElizaChat chat;

    public Assistant(Context context) {
        EventBus.get().register(this);
        chat = new ElizaChat();
        new Speaker(context);
        new VoiceRecognizer(context);
        new ConversationPresenter();
    }

    @Subscribe
    public void onNewQuery(NewQueryEvent e) {
        String response = chat.respond(e.getQuery());
        post(new NewResponseEvent(response));
    }

    public void shutdown() {
        post(new ShutdownEvent());
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }
}
