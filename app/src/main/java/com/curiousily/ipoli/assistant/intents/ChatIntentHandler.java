package com.curiousily.ipoli.assistant.intents;

import android.content.Context;

import com.curiousily.ipoli.assistant.ElizaChat;
import com.curiousily.ipoli.assistant.intents.events.IntentProcessedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class ChatIntentHandler extends AbstractIntentHandler {

    private ElizaChat chat = new ElizaChat();

    public ChatIntentHandler(Context context) {
        super(context);
    }

    @Override
    public void process(String task) {
        post(new IntentProcessedEvent(chat.respond(task)));
    }

    @Override
    public boolean canHandle(String task) {
        return true;
    }
}
