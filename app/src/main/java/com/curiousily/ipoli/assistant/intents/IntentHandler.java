package com.curiousily.ipoli.assistant.intents;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public interface IntentHandler {
    void process(String task);

    boolean canHandle(String task);
}
