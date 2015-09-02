package com.curiousily.ipoli.quest.services;

import com.curiousily.ipoli.app.api.APIClient;
import com.curiousily.ipoli.app.api.parameters.PathDate;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.events.CreateQuestEvent;
import com.curiousily.ipoli.schedule.DailySchedule;
import com.curiousily.ipoli.schedule.events.DailyQuestsLoadedEvent;
import com.curiousily.ipoli.schedule.events.LoadDailyQuestsEvent;
import com.curiousily.ipoli.schedule.ui.events.QuestRatedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.Callback;
import retrofit.RetrofitError;
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
        client.createQuest(e.quest, new Callback<Quest>() {
            @Override
            public void success(Quest quest, Response response) {
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    @Subscribe
    public void onLoadDailyQuests(LoadDailyQuestsEvent e) {

        client.getDailySchedule(new PathDate(e.scheduledFor), e.userId, new Callback<DailySchedule>() {
            @Override
            public void success(DailySchedule dailySchedule, Response response) {
                bus.post(new DailyQuestsLoadedEvent(dailySchedule.quests));
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    @Subscribe
    public void onQuestRated(QuestRatedEvent e) {

        client.rateQuest(e.quest, new Callback<Quest>() {
            @Override
            public void success(Quest quest, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}
