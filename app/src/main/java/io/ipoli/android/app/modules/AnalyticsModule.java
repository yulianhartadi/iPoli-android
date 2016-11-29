package io.ipoli.android.app.modules;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.Constants;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.FlurryAnalyticsService;
import io.ipoli.android.app.utils.LocalStorage;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AnalyticsModule {

    @Provides
    @Singleton
    public AnalyticsService provideAnalyticsService(Context context, LocalStorage localStorage) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        String playerId = localStorage.readString(Constants.KEY_PLAYER_ID);
        if (!TextUtils.isEmpty(playerId)) {
            firebaseAnalytics.setUserId(playerId);
        }
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);
        return new FlurryAnalyticsService(firebaseAnalytics);
    }
}
