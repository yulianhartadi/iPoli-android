package io.ipoli.android.app.modules;

import android.text.TextUtils;
import android.util.Log;

import com.amplitude.api.Amplitude;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.app.services.AmplitudeAnalyticsService;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.utils.LocalStorage;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AnalyticsModule {

    @Provides
    @Singleton
    public AnalyticsService provideAnalyticsService(LocalStorage localStorage) {
        String playerId = localStorage.readString(Constants.KEY_PLAYER_ID);
        if (!TextUtils.isEmpty(playerId)) {
            Amplitude.getInstance().setUserId(playerId);
        }
        if (BuildConfig.DEBUG) {
            Amplitude.getInstance().setLogLevel(Log.VERBOSE);
            Amplitude.getInstance().setOptOut(true);
        }
        return new AmplitudeAnalyticsService();
    }
}
