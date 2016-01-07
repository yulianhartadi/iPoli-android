package io.ipoli.assistant.modules;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.assistant.AnalyticsConstants;
import io.ipoli.assistant.BuildConfig;
import io.ipoli.assistant.services.AnalyticsService;
import io.ipoli.assistant.services.GoogleAnalyticsService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AnalyticsModule {

    @Provides
    @Singleton
    public AnalyticsService provideTodoManager(Context context) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        analytics.setDryRun(BuildConfig.DEBUG);
        Tracker tracker = analytics.newTracker(AnalyticsConstants.TRACKING_CODE);
        tracker.enableExceptionReporting(true);
        tracker.enableAutoActivityTracking(true);
        return new GoogleAnalyticsService(tracker);
    }
}
