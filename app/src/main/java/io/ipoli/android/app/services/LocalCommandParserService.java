package io.ipoli.android.app.services;

import android.support.annotation.Nullable;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
        return parse(command, new Command[]{validCommand});
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
                HashMap<String, Object> params = cmd.getParameters();
                String name = params.get("name").toString();
                bus.post(new NewQuestEvent(name, getStartTime(params), getDuration(params)));
                break;
            case ADD_TODAY_QUEST:
                params = cmd.getParameters();
                name = params.get("name").toString();
                bus.post(new NewTodayQuestEvent(name, getStartTime(params), getDuration(params)));
                break;
            case SHOW_QUESTS:
                bus.post(new ShowQuestsEvent());
                break;
            case RENAME:
                name = cmd.getParameters().get("name").toString();
                bus.post(new RenameAssistantEvent(name));
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

    private int getDuration(HashMap<String, Object> params) {
        return params.containsKey("duration") ? (int) params.get("duration") : -1;
    }

    @Nullable
    private Date getStartTime(HashMap<String, Object> params) {
        return params.containsKey("startTime") ? (Date) params.get("startTime") : null;
    }


}
