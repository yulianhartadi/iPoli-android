package io.ipoli.android.services;

import com.squareup.otto.Bus;

import io.ipoli.android.quest.events.NewQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class LocalCommandParserService implements CommandParserService {

    public static String ADD_QUEST_COMMAND = "add quest";
    private final Bus bus;

    public LocalCommandParserService(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void parse(String command) {
        String c = command.trim();
        String lc = c.toLowerCase();
        if (c.startsWith(ADD_QUEST_COMMAND)) {
            int startIndex = lc.indexOf(ADD_QUEST_COMMAND);
            int endIndex = startIndex + ADD_QUEST_COMMAND.length() + 1;
            bus.post(new NewQuestEvent(c.substring(endIndex)));
        }
    }
}
