package com.curiousily.ipoli.assistant.handlers;

import com.curiousily.ipoli.assistant.handlers.intents.Intent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public interface IntentHandler {
    void process(Intent intent);

    boolean canHandle(Intent intent);
}
