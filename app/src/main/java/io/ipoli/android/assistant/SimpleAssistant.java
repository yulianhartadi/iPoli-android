package io.ipoli.android.assistant;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestPersistenceService;
import io.ipoli.android.quest.events.NewQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class SimpleAssistant implements Assistant {
    
    private final QuestPersistenceService questPersistenceService;
    private final Bus eventBus;

    public SimpleAssistant(QuestPersistenceService questPersistenceService, Bus eventBus) {
        this.questPersistenceService = questPersistenceService;
        this.eventBus = eventBus;
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = new Quest(e.name);
        questPersistenceService.save(quest);
        eventBus.post(new AssistantReplyEvent("Your quest has been added!"));
    }
}
