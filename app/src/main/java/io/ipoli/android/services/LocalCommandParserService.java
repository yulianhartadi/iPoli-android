package io.ipoli.android.services;

import android.util.Log;

import com.squareup.otto.Bus;

import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.quest.events.NewQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class LocalCommandParserService implements CommandParserService {

    public static String ADD_QUEST_COMMAND = "add quest";
    public static String RENAME_COMMAND = "rename";
    public static String PLAN_TODAY_COMMAND = "plan today";

    private final Bus bus;

    public LocalCommandParserService(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void parse(String command) {
        String c = command.trim();
        String lc = c.toLowerCase();
        Log.d("CommandParser", lc);
        if (lc.startsWith(ADD_QUEST_COMMAND)) {
            bus.post(new NewQuestEvent(c.substring(parameterStartIndex(lc, ADD_QUEST_COMMAND))));
        } else if (lc.startsWith(RENAME_COMMAND)) {
            bus.post(new RenameAssistantEvent(c.substring(parameterStartIndex(lc, RENAME_COMMAND))));
        } else if (lc.startsWith(PLAN_TODAY_COMMAND)) {
            bus.post(new PlanTodayEvent());
        }
    }

    private int parameterStartIndex(String text, String command) {
        int startIndex = text.indexOf(command);
        return startIndex + command.length() + 1;
    }
}
