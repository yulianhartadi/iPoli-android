package io.ipoli.android.assistant;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestPersistenceService;
import io.ipoli.android.quest.events.NewQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class SimpleAssistant implements Assistant {

    @Inject
    QuestPersistenceService questPersistenceService;

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = new Quest(e.name);
        questPersistenceService.save(quest);
    }
}
