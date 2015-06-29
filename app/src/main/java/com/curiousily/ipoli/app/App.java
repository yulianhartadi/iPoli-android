package com.curiousily.ipoli.app;

import android.app.Application;

import com.curiousily.ipoli.AnalyticsConstants;
import com.curiousily.ipoli.BuildConfig;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.app.events.TrackEvent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

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
        EventBus.get().register(this);
    }

    @Subscribe
    public void onTrackEvent(TrackEvent e) {
        tracker.send(e.getEvent());
    }
}
