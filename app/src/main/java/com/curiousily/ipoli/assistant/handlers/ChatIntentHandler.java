package com.curiousily.ipoli.assistant.handlers;

import android.content.Context;

import com.curiousily.ipoli.assistant.handlers.events.IntentProcessedEvent;
import com.curiousily.ipoli.assistant.handlers.intents.ChatIntent;
import com.curiousily.ipoli.assistant.handlers.intents.Intent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class ChatIntentHandler extends AbstractIntentHandler {

    public ChatIntentHandler(Context context) {
        super(context);
    }

    @Override
    public void process(Intent intent) {
        ChatIntent chatIntent = (ChatIntent) intent;
        post(new IntentProcessedEvent(chatIntent.getResponse()));
    }

    @Override
    public boolean canHandle(Intent intent) {
        return true;
    }
}
