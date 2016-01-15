package io.ipoli.android.app.services;

import android.util.Log;

import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.List;

import io.ipoli.android.assistant.events.HelpEvent;
import io.ipoli.android.assistant.events.NewTodayQuestEvent;
import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.events.ShowQuestsEvent;
import io.ipoli.android.assistant.events.UnknownCommandEvent;
import io.ipoli.android.quest.events.NewQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class LocalCommandParserService implements CommandParserService {

    private final Bus bus;

    public LocalCommandParserService(Bus bus) {
        this.bus = bus;
    }

    @Override
    public boolean parse(String command) {
        return parse(command, Command.values());
    }

    @Override
    public boolean parse(String command, Command validCommand) {
        return parse(command, new Command[] {validCommand});
    }

    @Override
    public boolean parse(String command, Command[] validCommands) {
        Log.d("CommandParser", command);

        Command cmd = Command.parseText(command);
        List<Command> vc = Arrays.asList(validCommands);
        if (!vc.contains(cmd)) {
            return false;
        }
        switch (cmd) {
            case ADD_QUEST:
                bus.post(new NewQuestEvent(cmd.getParameterText()));
                break;
            case ADD_TODAY_QUEST:
                bus.post(new NewTodayQuestEvent(cmd.getParameterText()));
                break;
            case SHOW_QUESTS:
                bus.post(new ShowQuestsEvent());
                break;
            case RENAME:
                bus.post(new RenameAssistantEvent(cmd.getParameterText()));
                break;
            case PLAN_TODAY:
                bus.post(new PlanTodayEvent());
                break;
            case REVIEW_TODAY:
                bus.post(new ReviewTodayEvent());
                break;
            case HELP:
                bus.post(new HelpEvent());
                break;
            default:
                bus.post(new UnknownCommandEvent());
        }
        return true;
    }


}
