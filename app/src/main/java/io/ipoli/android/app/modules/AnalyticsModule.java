package io.ipoli.android.app.modules;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.AnalyticsConstants;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.GoogleAnalyticsService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AnalyticsModule {

    @Provides
    @Singleton
    public AnalyticsService provideAnalyticsService(Context context) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        analytics.setDryRun(BuildConfig.DEBUG);
        Tracker tracker = analytics.newTracker(AnalyticsConstants.TRACKING_CODE);
        tracker.enableExceptionReporting(true);
        tracker.enableAutoActivityTracking(true);
        return new GoogleAnalyticsService(tracker);
    }
}
