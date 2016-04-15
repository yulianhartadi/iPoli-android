package io.ipoli.android.app.modules;

import android.content.Context;

import com.flurry.android.FlurryAgent;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.AnalyticsConstants;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.FlurryAnalyticsService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AnalyticsModule {

    @Provides
    @Singleton
    public AnalyticsService provideAnalyticsService(Context context) {
        if(!BuildConfig.DEBUG) {
            new FlurryAgent.Builder()
                    .withLogEnabled(false)
                    .build(context, AnalyticsConstants.FLURRY_KEY);
        } else {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(context, "42");
        }

        return new FlurryAnalyticsService();
    }
}
