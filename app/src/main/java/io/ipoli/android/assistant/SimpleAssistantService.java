package io.ipoli.android.assistant;

import com.squareup.otto.Subscribe;

import io.ipoli.android.assistant.persistence.AssistantPersistenceService;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class SimpleAssistantService implements AssistantService {

    private final AssistantPersistenceService assistantPersistenceService;
    private final QuestPersistenceService questPersistenceService;
    private Assistant assistant;

    public SimpleAssistantService(AssistantPersistenceService assistantPersistenceService, QuestPersistenceService questPersistenceService) {
        this.assistantPersistenceService = assistantPersistenceService;
        this.questPersistenceService = questPersistenceService;
        assistant = assistantPersistenceService.find();
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = new Quest(e.name);
        quest.setDuration(e.duration);
        quest.setStartTime(e.startTime);
        if (e.dueDate != null) {
            quest.setStatus(Status.PLANNED.name());
            quest.setDue(e.dueDate);
        }
        questPersistenceService.save(quest);
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

}
