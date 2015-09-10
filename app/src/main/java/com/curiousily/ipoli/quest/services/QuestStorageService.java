package com.curiousily.ipoli.quest.services;

import com.curiousily.ipoli.app.api.APIClient;
import com.curiousily.ipoli.app.api.AsyncAPICallback;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.events.CreateQuestEvent;
import com.curiousily.ipoli.quest.services.events.QuestSavedEvent;
import com.curiousily.ipoli.schedule.events.QuestPostponedEvent;
import com.curiousily.ipoli.schedule.events.QuestRatedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.client.Response;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/13/15.
 */
public class QuestStorageService {
    private final APIClient client;
    private final Bus bus;

    public QuestStorageService(APIClient client, Bus bus) {
        this.client = client;
        this.bus = bus;
    }

    @Subscribe
    public void onCreateQuest(CreateQuestEvent e) {
        client.createQuest(e.quest, new AsyncAPICallback<Quest>() {
            @Override
            public void success(Quest quest, Response response) {
                bus.post(new QuestSavedEvent(quest));
            }

        });
    }

    @Subscribe
    public void onQuestRated(QuestRatedEvent e) {

        client.updateQuest(e.quest, new AsyncAPICallback<Quest>() {
            @Override
            public void success(Quest quest, Response response) {

            }
        });
    }

    @Subscribe
    public void onQuestPostponed(QuestPostponedEvent e) {
        client.updateQuest(e.quest, new AsyncAPICallback<Quest>() {
            @Override
            public void success(Quest quest, Response response) {

            }
        });
    }

}
