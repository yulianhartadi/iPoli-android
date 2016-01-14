package io.ipoli.android.assistant;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.ipoli.android.R;
import io.ipoli.android.app.services.Command;
import io.ipoli.android.app.services.CommandParserService;
import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.assistant.events.AssistantStartActivityEvent;
import io.ipoli.android.assistant.events.HelpEvent;
import io.ipoli.android.assistant.events.NewTodayQuestEvent;
import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.events.ShowQuestsEvent;
import io.ipoli.android.assistant.events.UnknownCommandEvent;
import io.ipoli.android.assistant.persistence.AssistantPersistenceService;
import io.ipoli.android.quest.PlanDayActivity;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestListActivity;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class SimpleAssistantService implements AssistantService {

    private final AssistantPersistenceService assistantPersistenceService;
    private final QuestPersistenceService questPersistenceService;
    private final CommandParserService commandParserService;
    private final Bus eventBus;
    private final Context context;
    private Assistant assistant;

    public SimpleAssistantService(AssistantPersistenceService assistantPersistenceService, QuestPersistenceService questPersistenceService, CommandParserService commandParserService, Bus eventBus, Context context) {
        this.assistantPersistenceService = assistantPersistenceService;
        this.questPersistenceService = questPersistenceService;
        this.commandParserService = commandParserService;
        this.eventBus = eventBus;
        this.context = context;
        assistant = assistantPersistenceService.find();
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = new Quest(e.name);
        questPersistenceService.save(quest);
        reply(context.getString(R.string.new_quest_reply));
    }

    @Subscribe
    public void onNewTodayQuest(NewTodayQuestEvent e) {
        Quest quest = new Quest(e.name, Quest.Status.PLANNED.name(), new Date());
        questPersistenceService.save(quest);
        reply(context.getString(R.string.new_today_quest_reply));
    }

    @Subscribe
    public void onPlanToday(PlanTodayEvent e) {
        if (questPersistenceService.countAllUncompleted() > 0) {
            reply(context.getString(R.string.ok_reply));
            eventBus.post(new AssistantStartActivityEvent(PlanDayActivity.class));
        } else {
            reply(context.getString(R.string.empty_uncomplete_quests_reply));
        }
    }

    @Subscribe
    public void onShowQuests(ShowQuestsEvent e) {
        if (questPersistenceService.countAllPlannedForToday() > 0) {
            reply(context.getString(R.string.ok_reply));
            eventBus.post(new AssistantStartActivityEvent(QuestListActivity.class));
        } else {
            reply(context.getString(R.string.empty_quests_for_today_reply));
        }
    }

    @Subscribe
    public void onRenameAssistant(RenameAssistantEvent e) {
        reply(String.format(context.getString(R.string.new_name_reply), e.name));
    }

    @Subscribe
    public void onReviewToday(ReviewTodayEvent e) {
        List<Quest> quests = questPersistenceService.findAllForToday();
        List<String> questStatuses = new ArrayList<>();
        String txt = "Today you have: <br/><br/>";
        for (Quest q : quests) {
            Quest.Status s = Quest.Status.valueOf(q.getStatus());
            String qs = (s == Quest.Status.COMPLETED) ? "[x]" : "[ ]";
            qs += " " + q.getName();
            questStatuses.add(qs);
        }
        txt += TextUtils.join("<br/>", questStatuses);
        reply(txt);
    }

    @Subscribe
    public void onHelp(HelpEvent e) {
        List<Command> commands = new ArrayList<>(Arrays.asList(Command.values()));
        commands.remove(Command.UNKNOWN);

        String helpText = "Command me with these:<br/><br/>";
        List<String> commandTexts = new ArrayList<>();
        for (Command cmd : commands) {
            commandTexts.add("* <b>" + cmd.toString()
                    + " (" + context.getString(cmd.getShortCommandText()) + ")" + "</b>"
                    + " - " + context.getString(cmd.getHelpText()));
        }
        helpText += TextUtils.join("<br/>", commandTexts);
        reply(helpText);
    }

    @Subscribe
    public void onUnknownCommand(UnknownCommandEvent e) {
        String[] excuses = context.getResources().getStringArray(R.array.excuses);
        Random r = new Random();
        String excuse = excuses[r.nextInt(excuses.length)];
        reply(excuse);
    }

    @Override
    public Assistant getAssistant() {
        return assistant;
    }

    @Override
    public void changeAvatar(String newAvatar) {
        assistant.setAvatar(newAvatar);
        assistant = assistantPersistenceService.save(assistant);
    }

    @Override
    public void onPlayerMessage(String text) {
        Log.d("text", text);
        Log.d("State", assistant.getState());
        switch (Assistant.State.valueOf(assistant.getState())) {
            case TUTORIAL_START:
                reply("It's nice to meet you! :)");
                changeState(Assistant.State.TUTORIAL_RENAME);
                break;
            case TUTORIAL_RENAME:
                boolean isValidCommand = commandParserService.parse(text, Command.RENAME);
                if (isValidCommand) {
                    changeState(Assistant.State.TUTORIAL_CHANGE_ASSISTANT_AVATAR);
                } else {
                    reply("You have to rename me to continue");
                }
                break;
            case TUTORIAL_CHANGE_ASSISTANT_AVATAR:
                reply("Wow! I look so cool!");
                changeState(Assistant.State.TUTORIAL_CHANGE_PLAYER_AVATAR);
                break;
            case TUTORIAL_CHANGE_PLAYER_AVATAR:
                reply("You look cute! ;)");
                changeState(Assistant.State.TUTORIAL_ADD_QUEST);
                break;
            case TUTORIAL_ADD_QUEST:
                isValidCommand = commandParserService.parse(text, Command.ADD_QUEST);
                if (isValidCommand) {
                    changeState(Assistant.State.TUTORIAL_PLAN_TODAY);
                } else {
                    reply("You have to add a quest to continue");
                }
                break;
            case TUTORIAL_PLAN_TODAY:
                isValidCommand = commandParserService.parse(text, Command.PLAN_TODAY);
                if (isValidCommand) {
                    changeState(Assistant.State.TUTORIAL_ADD_TODAY_QUEST);
                } else {
                    reply("You have to plan your day to continue");
                }
                break;
            case TUTORIAL_ADD_TODAY_QUEST:

                break;
            case NORMAL:
                commandParserService.parse(text);
                break;
        }
    }

    private void reply(String message) {
        eventBus.post(new AssistantReplyEvent(message));
    }

    private void onStateChanged(Assistant.State newState) {
        switch (newState) {
            case TUTORIAL_RENAME:
                reply("You can give me new name by typing <b>rename</b> (e.g. rename Katniss)");
                break;
            case TUTORIAL_CHANGE_ASSISTANT_AVATAR:
                reply("Tap on my avatar to change it. Say when you are ready!");
                break;
            case TUTORIAL_CHANGE_PLAYER_AVATAR:
                reply("Tap on your avatar to change it. Say when you are ready!");
                break;
            case TUTORIAL_ADD_QUEST:
                reply("Time to add your first quest! Type <b>add quest</b> (e.g. add quest Workout)");
                break;
            case TUTORIAL_PLAN_TODAY:
                reply("Plan you day by typing <b>plan today</b>");
                reply("Select which quests you want to do today and tap the save button");
                break;
            case TUTORIAL_ADD_TODAY_QUEST:

                break;
        }
    }

    private void changeState(Assistant.State newState) {
        assistant.setState(newState.name());
        assistant = assistantPersistenceService.save(assistant);
        onStateChanged(newState);
    }

    @Override
    public void start() {
        switch (Assistant.State.valueOf(assistant.getState())) {
            case TUTORIAL_START:
                onTutorialStart();
                break;
            default:
                reply("Nice to see you again! I am ready to continue!");
        }
    }

    private void onTutorialStart() {
        reply("Hello! I am iPoli. I am your personal sidekick!");
        reply("What is your name?");
    }
}
