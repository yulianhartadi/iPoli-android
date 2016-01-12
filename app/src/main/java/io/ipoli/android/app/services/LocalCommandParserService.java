package io.ipoli.android.app.services;

import android.util.Log;

import com.squareup.otto.Bus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ipoli.android.R;
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

    public enum Command {
        ADD_QUEST("(add quest|aq) (\\w[\\s|\\w]*)", R.string.short_cmd_add_quest, R.string.desc_cmd_add_quest),
        ADD_TODAY_QUEST("(add today quest|atq) (\\w[\\s|\\w]*)", R.string.short_cmd_add_today_quest, R.string.desc_cmd_add_today_quest),
        SHOW_QUESTS("(show quests|sq)", R.string.short_cmd_show_quests, R.string.desc_cmd_show_quests),
        PLAN_TODAY("(plan today|pt)", R.string.short_cmd_plan_today, R.string.desc_cmd_plan_today),
        REVIEW_TODAY("(review today|rt)", R.string.short_cmd_review_today, R.string.desc_cmd_review_today),
        RENAME("(rename|re) (\\w+)", R.string.short_cmd_rename, R.string.desc_cmd_rename),
        HELP("(help|h)", R.string.short_cmd_help, R.string.desc_cmd_help),
        UNKNOWN("", 0, 0);

        private final String pattern;
        private final int shortCommandText;
        private final int helpText;
        private String parameterText;

        Command(String pattern, int shortCommandText, int helpText) {
            this.pattern = pattern;
            this.shortCommandText = shortCommandText;
            this.helpText = helpText;
            this.parameterText = "";
        }

        public int getHelpText() {
            return helpText;
        }

        public String getParameterText() {
            return parameterText;
        }

        public int getShortCommandText() {
            return shortCommandText;
        }

        @Override
        public String toString() {
            return this.name().toLowerCase().replace("_", " ");
        }

        public static Command parseText(String text) {

            String normalized = text.trim().replaceAll("\\s+", " ");
            for (Command cmd : values()) {
                Pattern pattern = Pattern.compile(cmd.pattern);
                Matcher m = pattern.matcher(normalized);
                if (m.find()) {
                    Log.d("Group", m.groupCount() + "");
                    if (m.groupCount() > 1) {
                        cmd.parameterText = m.group(2);
                    }
                    return cmd;
                }
            }
            return UNKNOWN;
        }
    }

    private final Bus bus;

    public LocalCommandParserService(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void parse(String command) {
        Log.d("CommandParser", command);

        Command cmd = Command.parseText(command);
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
    }


}
