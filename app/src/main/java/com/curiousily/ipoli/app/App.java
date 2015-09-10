package com.curiousily.ipoli.app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.curiousily.ipoli.APIConstants;
import com.curiousily.ipoli.AnalyticsConstants;
import com.curiousily.ipoli.BuildConfig;
import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.app.api.APIClient;
import com.curiousily.ipoli.app.api.APIErrorHandler;
import com.curiousily.ipoli.app.events.TrackEvent;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.events.StartQuestEvent;
import com.curiousily.ipoli.quest.services.QuestService;
import com.curiousily.ipoli.quest.services.QuestStorageService;
import com.curiousily.ipoli.schedule.services.DailyScheduleStorageService;
import com.curiousily.ipoli.user.services.UserStorageService;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/15.
 */
public class App extends Application {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        initAnalytics();
    }

    private void initAnalytics() {
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setDryRun(BuildConfig.DEBUG);
        tracker = analytics.newTracker(AnalyticsConstants.TRACKING_CODE);
        tracker.enableExceptionReporting(true);
        tracker.enableAutoActivityTracking(true);
        Bus bus = EventBus.get();
        bus.register(this);
        APIClient client = buildAPI();
        bus.register(new QuestStorageService(client, bus));
        bus.register(new DailyScheduleStorageService(client, bus));
        bus.register(new UserStorageService(client, bus, this));
    }

    @Subscribe
    public void onStartQuest(StartQuestEvent e) {
        Intent intent = new Intent(this, QuestService.class);
        Quest quest = e.quest;
        intent.putExtra("name", quest.name);
        intent.putExtra("description", quest.description);
        intent.putExtra("duration", quest.duration);
        intent.putExtra("startTime", System.currentTimeMillis());
        scheduleUpdateAlarm(intent);
        startService(intent);
    }

    private void scheduleUpdateAlarm(Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getService(this, QuestService.UPDATE_PROGRESS_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        startAlarmManager(pendingIntent);
    }

    private void startAlarmManager(PendingIntent pendingIntent) {
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                TimeUnit.MINUTES.toMillis(1), pendingIntent);
    }

    private APIClient buildAPI() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat(Constants.DEFAULT_SERVER_DATETIME_FORMAT)
                .create();

        return new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(APIConstants.URL)
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new APIErrorHandler(this))
                .build()
                .create(APIClient.class);
    }

    @Subscribe
    public void onTrackEvent(TrackEvent e) {
        tracker.send(e.getEvent());
    }


}
