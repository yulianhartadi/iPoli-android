package io.ipoli.android.app.modules;

import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.SyncAndroidCalendarProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/6/17.
 */

@Module
public class AndroidCalendarModule {

    @Provides
    @Singleton
    public AndroidCalendarEventParser providedAndroidCalendarEventParser(SyncAndroidCalendarProvider syncAndroidCalendarProvider, Bus eventBus) {
        return new AndroidCalendarEventParser(syncAndroidCalendarProvider, eventBus);
    }

    @Provides
    @Singleton
    public SyncAndroidCalendarProvider provideSyncAndroidCalendarProvider(Context context) {
        return new SyncAndroidCalendarProvider(context);
    }
}
