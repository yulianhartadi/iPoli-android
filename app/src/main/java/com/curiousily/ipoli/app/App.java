package com.curiousily.ipoli.app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.curiousily.ipoli.APIConstants;
import com.curiousily.ipoli.AnalyticsConstants;
import com.curiousily.ipoli.BuildConfig;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.app.events.TrackEvent;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.services.QuestService;
import com.curiousily.ipoli.quest.services.QuestStorageService;
import com.curiousily.ipoli.ui.events.StartQuestEvent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;

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
        bus.register(new QuestStorageService(buildAPI(), bus));
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
        startQuestService(intent);
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

    private void startQuestService(Intent intent) {
        Intent i = new Intent(this, QuestService.class);
        i.putExtras(intent.getExtras());
        startService(i);
    }

    private APIClient buildAPI() {
        return new RestAdapter.Builder()
                .setEndpoint(APIConstants.URL)
                .build()
                .create(APIClient.class);
    }

    @Subscribe
    public void onTrackEvent(TrackEvent e) {
        tracker.send(e.getEvent());
    }
}
