package com.curiousily.ipoli.app;

import android.app.Application;

import com.curiousily.ipoli.APIConstants;
import com.curiousily.ipoli.AnalyticsConstants;
import com.curiousily.ipoli.BuildConfig;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.app.events.TrackEvent;
import com.curiousily.ipoli.quest.services.QuestStorageService;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.RestAdapter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/15.
 */
public class App extends Application {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    private QuestStorageService questService;

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
        questService = new QuestStorageService(buildAPI(), bus);
        bus.register(questService);
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
