package io.ipoli.android;

import javax.inject.Singleton;

import dagger.Component;
import io.ipoli.android.app.App;
import io.ipoli.android.modules.AnalyticsModule;
import io.ipoli.android.modules.AppModule;
import io.ipoli.android.modules.BusModule;
import io.ipoli.android.modules.CommandParserModule;
import io.ipoli.android.modules.PersistenceModule;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Singleton
@Component(
        modules = {
                AppModule.class,
                BusModule.class,
                PersistenceModule.class,
                AnalyticsModule.class,
                CommandParserModule.class
        }
)
public interface AppComponent {
    void inject(MainActivity activity);

    void inject(App app);
}

