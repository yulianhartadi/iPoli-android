package io.ipoli.android.assistant;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.assistant.events.HelpEvent;
import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.events.ShowQuestsEvent;
import io.ipoli.android.assistant.events.UnknownCommandEvent;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.app.services.LocalCommandParserService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class SimpleAssistantService implements AssistantService {

    private final QuestPersistenceService questPersistenceService;
    private final Bus eventBus;

    public SimpleAssistantService(QuestPersistenceService questPersistenceService, Bus eventBus) {
        this.questPersistenceService = questPersistenceService;
        this.eventBus = eventBus;
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = new Quest(e.name);
        questPersistenceService.save(quest);
        eventBus.post(new AssistantReplyEvent("Your quest has been added!"));
    }

    @Subscribe
    public void onShowQuests(ShowQuestsEvent e) {
        eventBus.post(new AssistantReplyEvent("Sure!"));
    }

    @Subscribe
    public void onRenameAssistant(RenameAssistantEvent e) {
        eventBus.post(new AssistantReplyEvent("Hi! My name is " + e.name));
    }

    @Subscribe
    public void onPlanToday(PlanTodayEvent e) {
        eventBus.post(new AssistantReplyEvent("Sure!"));
    }

    @Subscribe
    public void onReviewToday(ReviewTodayEvent e) {
        eventBus.post(new AssistantReplyEvent("Sure!"));
    }

    @Subscribe
    public void onHelp(HelpEvent e) {
        LocalCommandParserService.Command[] commands = LocalCommandParserService.Command.values();
        String helpText = "";
        for (LocalCommandParserService.Command cmd : commands) {
            helpText += cmd.toString() + " \n";
        }
        eventBus.post(new AssistantReplyEvent(helpText));
    }

    @Subscribe
    public void onUnknownCommand(UnknownCommandEvent e) {

    }
}
