package com.curiousily.ipoli.schedule.services;

import com.curiousily.ipoli.app.api.APIClient;
import com.curiousily.ipoli.app.api.AsyncAPICallback;
import com.curiousily.ipoli.app.api.parameters.PathDate;
import com.curiousily.ipoli.schedule.DailySchedule;
import com.curiousily.ipoli.schedule.events.DailyScheduleLoadedEvent;
import com.curiousily.ipoli.schedule.events.LoadDailyScheduleEvent;
import com.curiousily.ipoli.schedule.events.UpdateDailyScheduleEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/8/15.
 */
public class DailyScheduleStorageService {

    private final APIClient client;
    private final Bus bus;

    public DailyScheduleStorageService(APIClient client, Bus bus) {
        this.client = client;
        this.bus = bus;
    }

    @Subscribe
    public void onLoadDailySchedule(LoadDailyScheduleEvent e) {

        client.getDailySchedule(new PathDate(e.scheduledFor), e.userId, new AsyncAPICallback<DailySchedule>() {
            @Override
            public void success(DailySchedule dailySchedule, Response response) {
                bus.post(new DailyScheduleLoadedEvent(dailySchedule));
            }
        });
    }

    @Subscribe
    public void onUpdateDailySchedule(UpdateDailyScheduleEvent e) {
        client.updateSchedule(e.schedule, new AsyncAPICallback<DailySchedule>() {
            @Override
            public void success(DailySchedule schedule, Response response) {

            }
        });
    }
}
