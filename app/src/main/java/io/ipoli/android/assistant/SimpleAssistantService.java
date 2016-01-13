package io.ipoli.android.assistant;

import android.content.Context;
import android.text.TextUtils;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.ipoli.android.R;
import io.ipoli.android.app.services.Command;
import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.assistant.events.HelpEvent;
import io.ipoli.android.assistant.events.NewTodayQuestEvent;
import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.events.ShowQuestsEvent;
import io.ipoli.android.assistant.events.UnknownCommandEvent;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class SimpleAssistantService implements AssistantService {

    private final QuestPersistenceService questPersistenceService;
    private final Bus eventBus;
    private final Context context;

    public SimpleAssistantService(QuestPersistenceService questPersistenceService, Bus eventBus, Context context) {
        this.questPersistenceService = questPersistenceService;
        this.eventBus = eventBus;
        this.context = context;
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = new Quest(e.name);
        questPersistenceService.save(quest);
        eventBus.post(new AssistantReplyEvent(context.getString(R.string.new_quest_reply)));
    }

    @Subscribe
    public void onNewTodayQuest(NewTodayQuestEvent e) {
        Quest quest = new Quest(e.name, Quest.Status.PLANNED.name(), new Date());
        questPersistenceService.save(quest);
        eventBus.post(new AssistantReplyEvent(context.getString(R.string.new_today_quest_reply)));
    }

    @Subscribe
    public void onShowQuests(ShowQuestsEvent e) {
        eventBus.post(new AssistantReplyEvent(context.getString(R.string.ok_reply)));
    }

    @Subscribe
    public void onRenameAssistant(RenameAssistantEvent e) {
        eventBus.post(new AssistantReplyEvent(String.format(context.getString(R.string.new_name_reply), e.name)));
    }

    @Subscribe
    public void onPlanToday(PlanTodayEvent e) {
        eventBus.post(new AssistantReplyEvent(context.getString(R.string.ok_reply)));
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
        eventBus.post(new AssistantReplyEvent(txt));
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
        eventBus.post(new AssistantReplyEvent(helpText));
    }

    @Subscribe
    public void onUnknownCommand(UnknownCommandEvent e) {
        String[] excuses = context.getResources().getStringArray(R.array.excuses);
        Random r = new Random();
        String excuse = excuses[r.nextInt(excuses.length)];
        eventBus.post(new AssistantReplyEvent(excuse));
    }
}
